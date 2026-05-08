package xiaogj.xzagent.service;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import xiaogj.xzagent.infrastructure.session.MysqlAgentSession;
import xiaogj.xzagent.repository.A2uiSurfaceRepository;
import xiaogj.xzagent.repository.AgentRunRepository;
import xiaogj.xzagent.repository.SessionHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentRunFinalizationService {

    private static final Logger log = LoggerFactory.getLogger(AgentRunFinalizationService.class);
    private static final String MESSAGE_SESSION_KEY = "autoContextMemory_originalMessages";

    private final SessionActiveSkillService sessionActiveSkillService;
    private final AgentRunRepository agentRunRepository;
    private final A2uiSurfaceMessageBindingService a2uiSurfaceMessageBindingService;
    private final A2uiSurfaceRepository a2uiSurfaceRepository;
    private final SessionHistoryRepository sessionHistoryRepository;
    private final MeterRegistry meterRegistry;

    public AgentRunFinalizationService(
            SessionActiveSkillService sessionActiveSkillService,
            AgentRunRepository agentRunRepository,
            A2uiSurfaceMessageBindingService a2uiSurfaceMessageBindingService,
            A2uiSurfaceRepository a2uiSurfaceRepository,
            SessionHistoryRepository sessionHistoryRepository,
            MeterRegistry meterRegistry) {
        this.sessionActiveSkillService = sessionActiveSkillService;
        this.agentRunRepository = agentRunRepository;
        this.a2uiSurfaceMessageBindingService = a2uiSurfaceMessageBindingService;
        this.a2uiSurfaceRepository = a2uiSurfaceRepository;
        this.sessionHistoryRepository = sessionHistoryRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public void complete(String channel, AgentRuntime runtime, String runId, String generateReason) {
        complete(channel, runtime, runId, generateReason, null);
    }

    @Transactional
    public void complete(String channel, AgentRuntime runtime, String runId, String generateReason, Long lastEventSeq) {
        long startedAt = System.nanoTime();
        agentRunRepository.markFinalizing(runId, lastEventSeq);
        runtime.agent().saveTo(runtime.session(), runtime.sessionKey());
        bindDirtySurfaces(runtime);
        rebuildVisibleHistory(runtime);
        sessionActiveSkillService.save(runtime.sessionId(), runtime.skillBox());
        agentRunRepository.markCompleted(runId, generateReason, lastEventSeq);
        record(channel, "completed", startedAt);
    }

    @Transactional
    public void cancelAgui(AgentRuntime runtime, String runId) {
        cancelAgui(runtime, runId, null);
    }

    @Transactional
    public void cancelAgui(AgentRuntime runtime, String runId, Long lastEventSeq) {
        finalizeInterruptedRun("agui", runtime, runId, "AGUI stream cancelled by client", "cancelled", lastEventSeq);
    }

    @Transactional
    public void stopA2a(AgentRuntime runtime, String runId) {
        stopA2a(runtime, runId, null);
    }

    @Transactional
    public void stopA2a(AgentRuntime runtime, String runId, Long lastEventSeq) {
        finalizeInterruptedRun("a2a", runtime, runId, "A2A stream stopped by client", "stopped", lastEventSeq);
    }

    @Transactional
    public void fail(String channel, AgentRuntime runtime, String runId, Throwable error) {
        fail(channel, runtime, runId, error, null);
    }

    @Transactional
    public void fail(String channel, AgentRuntime runtime, String runId, Throwable error, Long lastEventSeq) {
        long startedAt = System.nanoTime();
        agentRunRepository.markFinalizing(runId, lastEventSeq);
        try {
            runtime.agent().saveTo(runtime.session(), runtime.sessionKey());
            bindDirtySurfaces(runtime);
            rebuildVisibleHistory(runtime);
            sessionActiveSkillService.save(runtime.sessionId(), runtime.skillBox());
        } catch (RuntimeException finalizationError) {
            log.error(
                    "失败链路收口异常: channel={}, runId={}, sessionId={}, errorType={}, message={}",
                    channel,
                    runId,
                    runtime.sessionId(),
                    finalizationError.getClass().getSimpleName(),
                    finalizationError.getMessage(),
                    finalizationError);
        } finally {
            agentRunRepository.markFailed(runId, error.getMessage(), lastEventSeq);
            record(channel, "failed", startedAt);
        }
    }

    private void finalizeInterruptedRun(
            String channel,
            AgentRuntime runtime,
            String runId,
            String finishedReason,
            String result,
            Long lastEventSeq) {
        long startedAt = System.nanoTime();
        agentRunRepository.markFinalizing(runId, lastEventSeq);
        try {
            runtime.agent().saveTo(runtime.session(), runtime.sessionKey());
            bindDirtySurfaces(runtime);
            rebuildVisibleHistory(runtime);
            sessionActiveSkillService.save(runtime.sessionId(), runtime.skillBox());
        } finally {
            agentRunRepository.markCancelled(runId, finishedReason, lastEventSeq);
            record(channel, result, startedAt);
        }
    }

    private void bindDirtySurfaces(AgentRuntime runtime) {
        if (runtime.a2uiSurfaceTrackingHook() == null) {
            return;
        }
        a2uiSurfaceMessageBindingService.bindDirtySurfacesToLastAssistantMessage(
                runtime.session(),
                runtime.sessionKey(),
                runtime.a2uiSurfaceTrackingHook().getDirtySurfaceIds());
        runtime.a2uiSurfaceTrackingHook().clearDirtySurfaceIds();
    }

    private void rebuildVisibleHistory(AgentRuntime runtime) {
        List<Msg> messages = runtime.session().getList(runtime.sessionKey(), MESSAGE_SESSION_KEY, Msg.class);
        Set<String> boundMessageIds = a2uiSurfaceRepository.findBoundMessageIds(runtime.sessionId());
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
        sessionHistoryRepository.replaceVisibleMessages(
                runtime.sessionId(),
                visibleMessages,
                runtime.session() instanceof MysqlAgentSession mysqlAgentSession
                        ? mysqlAgentSession.findUpdatedAt(runtime.sessionId()).orElse(java.time.Instant.now())
                        : java.time.Instant.now());
    }

    private boolean isHiddenFromHistory(Msg msg) {
        if (msg.getMetadata() != null && Boolean.TRUE.equals(msg.getMetadata().get("hiddenInHistory"))) {
            return true;
        }
        String messageId = msg.getId();
        return messageId != null
                && messageId.startsWith(A2uiMessageApplicationService.A2UI_ACTION_MESSAGE_ID_PREFIX);
    }

    private void record(String channel, String result, long startedAt) {
        meterRegistry.counter("xzagent.agent.run.finalization", "channel", channel, "result", result)
                .increment();
        meterRegistry.timer("xzagent.agent.run.finalization.duration", "channel", channel, "result", result)
                .record(System.nanoTime() - startedAt, TimeUnit.NANOSECONDS);
    }
}
