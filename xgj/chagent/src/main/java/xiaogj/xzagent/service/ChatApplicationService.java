package xiaogj.xzagent.service;

import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.state.SimpleSessionKey;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import xiaogj.xzagent.infrastructure.session.MysqlAgentSession;
import xiaogj.xzagent.model.A2uiSurfaceState;
import xiaogj.xzagent.repository.A2uiSurfaceRepository;
import xiaogj.xzagent.repository.SessionActiveSkillRepository;
import xiaogj.xzagent.repository.SessionHistoryRepository;
import xiaogj.xzagent.util.SessionIdValidator;
import xiaogj.xzagent.web.dto.SessionMessageResponse;
import xiaogj.xzagent.web.dto.SessionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class ChatApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ChatApplicationService.class);
    private static final int MAX_PAGE_SIZE = 100;
    private static final String SURFACE_SESSION_KEY = "a2ui_surfaces";
    private static final String MESSAGE_SESSION_KEY = "autoContextMemory_originalMessages";

    private final AgentFactory agentFactory;
    private final SessionActiveSkillService sessionActiveSkillService;
    private final SessionActiveSkillRepository sessionActiveSkillRepository;
    private final AgentRunFinalizationService agentRunFinalizationService;
    private final MysqlAgentSession session;
    private final A2uiSurfaceRepository a2uiSurfaceRepository;
    private final SessionHistoryRepository sessionHistoryRepository;

    public ChatApplicationService(
            AgentFactory agentFactory,
            SessionActiveSkillService sessionActiveSkillService,
            SessionActiveSkillRepository sessionActiveSkillRepository,
            AgentRunFinalizationService agentRunFinalizationService,
            MysqlAgentSession session,
            A2uiSurfaceRepository a2uiSurfaceRepository,
            SessionHistoryRepository sessionHistoryRepository) {
        this.agentFactory = agentFactory;
        this.sessionActiveSkillService = sessionActiveSkillService;
        this.sessionActiveSkillRepository = sessionActiveSkillRepository;
        this.agentRunFinalizationService = agentRunFinalizationService;
        this.session = session;
        this.a2uiSurfaceRepository = a2uiSurfaceRepository;
        this.sessionHistoryRepository = sessionHistoryRepository;
    }

    public String chat(String sessionId, String message, UserPrincipal principal) {
        String runId = UUID.randomUUID().toString();
        AgentRuntime runtime = agentFactory.create(
                sessionId,
                runId,
                principal.userId(),
                principal.username());
        log.info(
                "开始执行同步聊天: runId={}, sessionId={}, userId={}, username={}, messageLength={}",
                runId,
                runtime.sessionId(),
                principal.userId(),
                principal.username(),
                message != null ? message.length() : 0);
        try {
            Msg response = runtime.agent().call(Msg.builder().textContent(message).build()).block();
            agentRunFinalizationService.complete(
                    "chat-sync",
                    runtime,
                    runId,
                    response != null && response.getGenerateReason() != null
                            ? response.getGenerateReason().name()
                            : "UNKNOWN");
            log.info(
                    "同步聊天完成: runId={}, sessionId={}, userId={}, username={}, responseLength={}, reason={}",
                    runId,
                    runtime.sessionId(),
                    principal.userId(),
                    principal.username(),
                    response != null && response.getTextContent() != null
                            ? response.getTextContent().length()
                            : 0,
                    response != null && response.getGenerateReason() != null
                            ? response.getGenerateReason().name()
                            : "UNKNOWN");
            return response != null ? response.getTextContent() : "";
        } catch (RuntimeException ex) {
            agentRunFinalizationService.fail("chat-sync", runtime, runId, ex);
            log.error(
                    "同步聊天失败: runId={}, sessionId={}, userId={}, username={}, errorType={}, message={}",
                    runId,
                    runtime.sessionId(),
                    principal.userId(),
                    principal.username(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    public Flux<String> stream(String sessionId, String message, UserPrincipal principal) {
        String runId = UUID.randomUUID().toString();
        StreamOptions options = StreamOptions.builder()
                .eventTypes(EventType.REASONING, EventType.TOOL_RESULT)
                .incremental(true)
                .includeReasoningResult(false)
                .build();
        return Flux.defer(() -> {
                    AgentRuntime runtime = agentFactory.create(
                            sessionId,
                            runId,
                            principal.userId(),
                            principal.username());
                    log.info(
                            "开始执行流式聊天: runId={}, sessionId={}, userId={}, username={}, messageLength={}",
                            runId,
                            runtime.sessionId(),
                            principal.userId(),
                            principal.username(),
                            message != null ? message.length() : 0);
                    return runtime.agent()
                            .stream(Msg.builder().textContent(message).build(), options)
                            .doOnNext(event -> log.debug(
                                    "收到流式事件: runId={}, sessionId={}, userId={}, username={}, eventType={}, textLength={}",
                                    runId,
                                    runtime.sessionId(),
                                    principal.userId(),
                                    principal.username(),
                                    event.getType(),
                                    event.getMessage() != null && event.getMessage().getTextContent() != null
                                            ? event.getMessage().getTextContent().length()
                                            : 0))
                            .map(event -> event.getMessage().getTextContent())
                            .filter(text -> text != null && !text.isEmpty())
                            .doOnComplete(() -> {
                                agentRunFinalizationService.complete("chat-stream", runtime, runId, "STREAM_COMPLETED");
                                log.info(
                                        "流式聊天完成: runId={}, sessionId={}, userId={}, username={}",
                                        runId,
                                        runtime.sessionId(),
                                        principal.userId(),
                                        principal.username());
                            })
                            .doOnError(error -> {
                                agentRunFinalizationService.fail("chat-stream", runtime, runId, error);
                                log.error(
                                        "流式聊天失败: runId={}, sessionId={}, userId={}, username={}, errorType={}, message={}",
                                        runId,
                                        runtime.sessionId(),
                                        principal.userId(),
                                        principal.username(),
                                        error.getClass().getSimpleName(),
                                        error.getMessage(),
                                        error);
                            });
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public SessionResponse getSession(String rawSessionId, int page, int size) {
        String sessionId = SessionIdValidator.validate(rawSessionId);
        SimpleSessionKey sessionKey = SimpleSessionKey.of(sessionId);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        boolean migratedLegacySurfaces = syncLegacySurfaces(sessionId, sessionKey);
        java.time.Instant sessionUpdatedAt = session.findUpdatedAt(sessionId).orElse(null);
        SessionHistorySummary storedSummary = sessionHistoryRepository.findStoredSummary(sessionId).orElse(null);
        SessionHistorySummary indexSummary = sessionHistoryRepository.computeIndexSummary(sessionId);
        if (migratedLegacySurfaces || shouldRefreshReadModel(sessionUpdatedAt, storedSummary, indexSummary)) {
            List<SessionHistoryMessage> sourceVisibleMessages = loadSourceVisibleMessages(sessionId, sessionKey);
            sessionHistoryRepository.replaceVisibleMessages(sessionId, sourceVisibleMessages, sessionUpdatedAt);
        }
        SessionHistoryPage historyPage = sessionHistoryRepository.findPage(sessionId, safePage, safeSize);
        Map<String, List<A2uiSurfaceState>> surfacesByMsgId = loadSurfacesByMessageId(
                sessionId,
                historyPage.messages().stream().map(SessionHistoryMessage::messageId).toList());
        log.info(
                "查询会话快照: sessionId={}, page={}, size={}, totalCount={}",
                sessionId,
                safePage,
                safeSize,
                historyPage.totalCount());
        return new SessionResponse(
                sessionId,
                sessionActiveSkillRepository.findActiveSkillIds(sessionId),
                historyPage.messages().stream()
                        .map(message -> new SessionMessageResponse(
                                message.messageId(),
                                message.role(),
                                message.content(),
                                surfacesByMsgId.getOrDefault(message.messageId(), List.of())))
                        .toList(),
                historyPage.totalCount());
    }

    public void resetSession(String rawSessionId) {
        String sessionId = SessionIdValidator.validate(rawSessionId);
        session.delete(SimpleSessionKey.of(sessionId));
        a2uiSurfaceRepository.deleteBySessionId(sessionId);
        sessionHistoryRepository.deleteBySessionId(sessionId);
        sessionActiveSkillService.clear(sessionId);
    }

    private Map<String, List<A2uiSurfaceState>> loadSurfacesByMessageId(String sessionId, List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Map.of();
        }
        return a2uiSurfaceRepository.findBySessionIdAndLastUpdatedMsgIds(sessionId, messageIds).stream()
                .filter(surface -> surface.lastUpdatedMsgId() != null && !surface.lastUpdatedMsgId().isBlank())
                .collect(Collectors.groupingBy(
                        A2uiSurfaceState::lastUpdatedMsgId,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    private boolean syncLegacySurfaces(String sessionId, SimpleSessionKey sessionKey) {
        if (!a2uiSurfaceRepository.findBySessionId(sessionId).isEmpty()) {
            return false;
        }
        List<A2uiSurfaceState> legacySurfaces = session.getList(sessionKey, SURFACE_SESSION_KEY, A2uiSurfaceState.class);
        if (legacySurfaces.isEmpty()) {
            return false;
        }
        a2uiSurfaceRepository.upsertAll(sessionId, legacySurfaces);
        return true;
    }

    private List<SessionHistoryMessage> loadSourceVisibleMessages(String sessionId, SimpleSessionKey sessionKey) {
        return toVisibleMessages(
                session.getList(sessionKey, MESSAGE_SESSION_KEY, Msg.class),
                a2uiSurfaceRepository.findBoundMessageIds(sessionId));
    }

    private boolean shouldRefreshReadModel(
            java.time.Instant sessionUpdatedAt,
            SessionHistorySummary storedSummary,
            SessionHistorySummary indexSummary) {
        if (storedSummary == null) {
            return true;
        }
        if (sessionUpdatedAt != null) {
            java.time.Instant sourceUpdatedAt = storedSummary.sourceUpdatedAt();
            if (sourceUpdatedAt == null || sourceUpdatedAt.isBefore(sessionUpdatedAt)) {
                return true;
            }
        }
        return !storedSummary.structurallyMatches(indexSummary);
    }

    private List<SessionHistoryMessage> toVisibleMessages(List<Msg> messages, Set<String> boundMessageIds) {
        List<SessionHistoryMessage> visibleMessages = new ArrayList<>();
        int order = 0;
        for (Msg message : messages) {
            if (message.getRole() != MsgRole.USER && message.getRole() != MsgRole.ASSISTANT) {
                continue;
            }
            if (isHiddenFromHistory(message)) {
                continue;
            }
            boolean hasSurface = message.getId() != null && boundMessageIds.contains(message.getId());
            String textContent = message.getTextContent();
            if ((textContent == null || textContent.isBlank()) && !hasSurface) {
                continue;
            }
            String messageId = message.getId() != null && !message.getId().isBlank()
                    ? message.getId()
                    : "generated-" + order;
            visibleMessages.add(new SessionHistoryMessage(
                    messageId,
                    message.getRole() == MsgRole.USER ? "user" : "assistant",
                    message.getTextContent(),
                    order));
            order++;
        }
        return visibleMessages;
    }

    private boolean isHiddenFromHistory(Msg msg) {
        Map<String, Object> metadata = msg.getMetadata();
        if (metadata != null && Boolean.TRUE.equals(metadata.get("hiddenInHistory"))) {
            return true;
        }
        String messageId = msg.getId();
        return messageId != null
                && messageId.startsWith(A2uiMessageApplicationService.A2UI_ACTION_MESSAGE_ID_PREFIX);
    }
}
