package xiaogj.xzagent.web;

import io.agentscope.core.agui.adapter.AguiAdapterConfig;
import io.agentscope.core.agui.adapter.AguiAgentAdapter;
import io.agentscope.core.agui.event.AguiEvent;
import io.agentscope.core.agui.model.AguiMessage;
import io.agentscope.core.agui.model.RunAgentInput;
import io.agentscope.core.message.Msg;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import xiaogj.xzagent.service.A2uiEventPostProcessor;
import xiaogj.xzagent.service.A2uiMessageApplicationService;
import xiaogj.xzagent.service.AgentFactory;
import xiaogj.xzagent.service.AgentRunFinalizationService;
import xiaogj.xzagent.service.AgentRuntime;
import xiaogj.xzagent.service.DistributedRunCoordinator;
import xiaogj.xzagent.service.NodeIdentityProvider;
import xiaogj.xzagent.service.RunEventStreamStore;
import xiaogj.xzagent.service.RunHeartbeatService;
import xiaogj.xzagent.service.RunStreamRegistry;
import xiaogj.xzagent.service.UserPrincipal;
import xiaogj.xzagent.repository.SessionHistoryRepository;
import xiaogj.xzagent.util.SessionIdValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * 基于 AG-UI 协议的 Web 聊天入口。
 *
 * <p>这里显式不使用 AGUI starter 默认的 server-side memory 机制，而是继续复用
 * 现有 `MysqlAgentSession + /api/sessions` 作为唯一会话真相。也就是说：
 * <ul>
 *     <li>`threadId` 仅映射到现有 `sessionId`</li>
 *     <li>`runId` 表示一次前端发起的执行</li>
 *     <li>真实会话恢复和保存仍通过现有 AgentFactory / MysqlAgentSession 完成</li>
 * </ul>
 *
 * <h2>断线重连机制</h2>
 * <p>Agent 在后台独立执行，与前端 SSE 连接解耦：
 * <ul>
 *     <li><b>意外断线</b>（关闭页面、网络中断）：后台 Agent 继续运行；
 *         前端重新进入后调用 /agui/run，检测到同一 sessionId 仍有活跃运行，
 *         直接订阅已有热流，恢复流式输出。</li>
 *     <li><b>主动取消</b>：前端调用 /agui/sessions/{sessionId}/cancel，
 *         后端发送中断信号，Agent 在下一个检查点真正停止。</li>
 * </ul>
 */
@RestController
@RequestMapping("/xzagent/agui")
public class AguiChatController {

    private static final Logger log = LoggerFactory.getLogger(AguiChatController.class);
    private static final String A2UI_ACTION_PLACEHOLDER = "[A2UI_ACTION]";

    private final AgentFactory agentFactory;
    private final AgentRunFinalizationService agentRunFinalizationService;
    private final A2uiEventPostProcessor a2uiEventPostProcessor;
    private final A2uiMessageApplicationService a2uiMessageApplicationService;
    private final RunStreamRegistry runStreamRegistry;
    private final DistributedRunCoordinator distributedRunCoordinator;
    private final RunEventStreamStore runEventStreamStore;
    private final RunHeartbeatService runHeartbeatService;
    private final NodeIdentityProvider nodeIdentityProvider;
    private final SessionHistoryRepository sessionHistoryRepository;
    private final AguiAdapterConfig aguiAdapterConfig;

    public AguiChatController(
            AgentFactory agentFactory,
            AgentRunFinalizationService agentRunFinalizationService,
            A2uiEventPostProcessor a2uiEventPostProcessor,
            A2uiMessageApplicationService a2uiMessageApplicationService,
            RunStreamRegistry runStreamRegistry,
            DistributedRunCoordinator distributedRunCoordinator,
            RunEventStreamStore runEventStreamStore,
            RunHeartbeatService runHeartbeatService,
            NodeIdentityProvider nodeIdentityProvider,
            SessionHistoryRepository sessionHistoryRepository) {
        this.agentFactory = agentFactory;
        this.agentRunFinalizationService = agentRunFinalizationService;
        this.a2uiEventPostProcessor = a2uiEventPostProcessor;
        this.a2uiMessageApplicationService = a2uiMessageApplicationService;
        this.runStreamRegistry = runStreamRegistry;
        this.distributedRunCoordinator = distributedRunCoordinator;
        this.runEventStreamStore = runEventStreamStore;
        this.runHeartbeatService = runHeartbeatService;
        this.nodeIdentityProvider = nodeIdentityProvider;
        this.sessionHistoryRepository = sessionHistoryRepository;
        this.aguiAdapterConfig = AguiAdapterConfig.builder()
                .emitStateEvents(false)
                .emitToolCallArgs(false)
                .enableReasoning(false)
                .defaultAgentId("default")
                .build();
    }

    /**
     * 运行默认聊天 Agent，或重连到正在执行的 Agent。
     *
     * <h3>新建运行</h3>
     * <p>若该 session 当前无活跃运行：启动 Agent 后台执行，返回 SSE 热流订阅。
     * 前端意外断开后，后台 Agent 继续运行；再次调用本接口时将进入重连分支。
     *
     * <h3>重连</h3>
     * <p>若该 session 已有正在执行的 Agent（前端意外断开后重进页面）：
     * 直接订阅已有热流，恢复流式输出，不重新启动 Agent。
     * 前端在重连前可从历史接口加载已完成的消息，本接口只补充断线后的新增事件。
     */
    @PostMapping(path = "/run", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> run(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RunAgentInput input) {
        String sessionId = SessionIdValidator.validate(input.getThreadId());
        String runId = input.getRunId();

        // ── 重连检测 ──────────────────────────────────────────────────────────
        // 若该 session 已有活跃运行（Agent 仍在后台执行），直接订阅其热流。
        // 前端不需要重放历史事件，已完成的消息由历史接口负责展示。
        Optional<RunStreamRegistry.ActiveRun> existingRun = runStreamRegistry.find(sessionId);
        if (existingRun.isPresent()) {
            log.info("检测到同节点重连请求，订阅已有运行流: username={}, sessionId={}, activeRunId={}",
                    principal.username(), sessionId, existingRun.get().runtime().runId());
            return toSseFlux(existingRun.get().sink().asFlux());
        }

        // ── 新建运行 ──────────────────────────────────────────────────────────
        List<AguiMessage> pendingClientMessages = toAguiMessages(
                a2uiMessageApplicationService.listClientMessages(sessionId));
        boolean hasPendingClientMessages = !pendingClientMessages.isEmpty();
        RunAgentInput effectiveInput = buildEffectiveInput(input, sessionId, pendingClientMessages);

        log.info("收到 AGUI 聊天请求: username={}, sessionId={}, runId={}, messageCount={}",
                principal.username(), sessionId, runId, effectiveInput.getMessages().size());

        // run 开始前预存用户消息，使重连时可立即展示（run 结束后 rebuildVisibleHistory 会完整覆盖）
        AguiMessage pendingUserMessage = extractLatestUserMessage(effectiveInput);
        if (pendingUserMessage != null) {
            try {
                sessionHistoryRepository.appendPendingUserMessage(
                        sessionId, pendingUserMessage.getId(), pendingUserMessage.getContent());
            } catch (Exception e) {
                log.warn("预存用户消息失败（不影响运行）: sessionId={}, error={}", sessionId, e.getMessage());
            }
        }

        boolean claimed = distributedRunCoordinator.tryClaim(
                sessionId,
                runId,
                nodeIdentityProvider.currentNodeId(),
                nodeIdentityProvider.internalBaseUrl());
        if (!claimed) {
            Optional<RunStreamRegistry.ActiveRun> activeRun = runStreamRegistry.find(sessionId);
            if (activeRun.isPresent()) {
                log.info("检测到并发重连请求，复用当前节点活跃流: username={}, sessionId={}, runId={}",
                        principal.username(), sessionId, activeRun.get().runtime().runId());
                return toSseFlux(activeRun.get().sink().asFlux());
            }
            throw new IllegalStateException("该 session 正在其他节点执行中，请改用重连接口继续订阅");
        }

        // 创建多播热流：live 事件只发给当前在线订阅者，完整事件仍会写入 Redis Stream 供断线补发。
        Sinks.Many<RunEventStreamStore.StoredRunEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 用 AtomicReference 跨 Flux.defer 边界捕获运行时实例，供 subscribe 回调使用
        AtomicReference<AgentRuntime> runtimeRef = new AtomicReference<>();
        AtomicLong lastEventSeqRef = new AtomicLong(0L);

        // 注册到活跃运行表，使重连检测和取消接口均可找到此运行
        // 注意：必须在后台订阅之前注册，否则存在极短的竞态窗口
        // （暂存占位，runtime 在后台线程中创建后通过 runtimeRef 持有）
        // 实际注册在 Flux.defer 内部完成，见下方后台订阅逻辑

        // ── 后台独立执行 Agent ──────────────────────────────────────────────
        // subscribe 与 SSE 订阅完全解耦：前端 SSE 断开只取消 sink.asFlux() 的订阅，
        // 不影响此处的后台 subscribe，Agent 将持续运行至正常完成、出错或被主动取消。
        Flux.defer(() -> {
                    // agentFactory.create() 含 DB 读取（loadIfExists），在 boundedElastic 线程执行
                    AgentRuntime runtime = agentFactory.create(
                            sessionId, runId, principal.userId(), principal.username());
                    runtimeRef.set(runtime);
                    // 注册到活跃运行表（在后台线程中，runtime 已就绪）
                    runStreamRegistry.register(sessionId, new RunStreamRegistry.ActiveRun(runtime, sink));
                    runHeartbeatService.start(runId, lastEventSeqRef::get);
                    AguiAgentAdapter adapter = new AguiAgentAdapter(runtime.agent(), aguiAdapterConfig);
                    return a2uiEventPostProcessor.process(adapter.run(effectiveInput));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        // onNext：将每个 AGUI 事件推送到热流
                        // 若前端当前已断开（无订阅者），tryEmitNext 返回 FAIL_ZERO_SUBSCRIBER，事件被丢弃
                        event -> {
                            long seq = lastEventSeqRef.incrementAndGet();
                            RunEventStreamStore.StoredRunEvent storedEvent = runEventStreamStore.append(runId, seq, event);
                            distributedRunCoordinator.updateLastEventSeq(runId, seq);
                            Sinks.EmitResult result = sink.tryEmitNext(storedEvent);
                            if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
                                log.warn("事件推送热流失败（非预期）: runId={}, result={}", runId, result);
                            }
                        },
                        // onError：Agent 执行出错，立即关闭 SSE 流，异步执行 DB 收口
                        error -> {
                            log.error("AGUI 聊天失败（后台）: username={}, sessionId={}, runId={}, errorType={}, message={}",
                                    principal.username(), sessionId, runId,
                                    error.getClass().getSimpleName(), error.getMessage(), error);
                            runHeartbeatService.stop(runId);
                            distributedRunCoordinator.updateStatus(runId, xiaogj.xzagent.model.AgentRunStatus.FINALIZING);
                            runStreamRegistry.remove(sessionId);
                            if (hasPendingClientMessages) {
                                a2uiMessageApplicationService.clearClientMessages(sessionId);
                            }
                            String errorMsg = error.getMessage() != null ? error.getMessage() : "AGUI 请求失败";
                            long errorSeq = lastEventSeqRef.incrementAndGet();
                            sink.tryEmitNext(runEventStreamStore.append(
                                    runId,
                                    errorSeq,
                                    new AguiEvent.Raw(sessionId, runId, Map.of("error", errorMsg))));
                            distributedRunCoordinator.updateLastEventSeq(runId, errorSeq);
                            long finishedSeq = lastEventSeqRef.incrementAndGet();
                            sink.tryEmitNext(runEventStreamStore.append(
                                    runId,
                                    finishedSeq,
                                    new AguiEvent.RunFinished(sessionId, runId)));
                            distributedRunCoordinator.updateLastEventSeq(runId, finishedSeq);
                            distributedRunCoordinator.updateStatus(runId, xiaogj.xzagent.model.AgentRunStatus.FAILED);
                            sink.tryEmitComplete();
                            runEventStreamStore.expire(runId);
                            AgentRuntime runtime = runtimeRef.get();
                            if (runtime != null) {
                                Schedulers.boundedElastic().schedule(() -> {
                                    try {
                                        agentRunFinalizationService.fail("agui", runtime, runId, error, finishedSeq);
                                    } catch (Exception e) {
                                        log.error("AGUI 失败收口异常: sessionId={}, runId={}, error={}",
                                                sessionId, runId, e.getMessage(), e);
                                    } finally {
                                        distributedRunCoordinator.clear(sessionId, runId);
                                    }
                                });
                            } else {
                                distributedRunCoordinator.clear(sessionId, runId);
                            }
                        },
                        // onComplete：先发布最终事件，再异步执行 DB 收口，最后清理协调元数据
                        () -> {
                            log.info("AGUI 聊天完成（后台）: username={}, sessionId={}, runId={}",
                                    principal.username(), sessionId, runId);
                            runHeartbeatService.stop(runId);
                            distributedRunCoordinator.updateStatus(runId, xiaogj.xzagent.model.AgentRunStatus.FINALIZING);
                            runStreamRegistry.remove(sessionId);
                            if (hasPendingClientMessages) {
                                a2uiMessageApplicationService.clearClientMessages(sessionId);
                            }
                            long finishedSeq = lastEventSeqRef.incrementAndGet();
                            sink.tryEmitNext(runEventStreamStore.append(
                                    runId,
                                    finishedSeq,
                                    new AguiEvent.RunFinished(sessionId, runId)));
                            distributedRunCoordinator.updateLastEventSeq(runId, finishedSeq);
                            distributedRunCoordinator.updateStatus(runId, xiaogj.xzagent.model.AgentRunStatus.COMPLETED);
                            sink.tryEmitComplete();
                            runEventStreamStore.expire(runId);
                            AgentRuntime runtime = runtimeRef.get();
                            if (runtime != null) {
                                Schedulers.boundedElastic().schedule(() -> {
                                    try {
                                        agentRunFinalizationService.complete("agui", runtime, runId, "AGUI_COMPLETED", finishedSeq);
                                    } catch (Exception e) {
                                        log.error("AGUI 完成收口异常: sessionId={}, runId={}, error={}",
                                                sessionId, runId, e.getMessage(), e);
                                    } finally {
                                        distributedRunCoordinator.clear(sessionId, runId);
                                    }
                                });
                            } else {
                                distributedRunCoordinator.clear(sessionId, runId);
                            }
                        });

        if (!hasPendingClientMessages) {
            a2uiMessageApplicationService.recordClearClientMessagesSkipped();
        }

        // SSE 订阅热流。前端断开时仅取消此订阅，不影响后台 Agent 执行。
        return toSseFlux(sink.asFlux());
    }

    /**
     * 保留最新用户消息，并把待消费的 A2UI client message 一并注入本轮请求。
     */
    private RunAgentInput buildEffectiveInput(
            RunAgentInput input,
            String sessionId,
            List<AguiMessage> pendingClientMessages) {
        AguiMessage latestUserMessage = extractLatestUserMessage(input);
        List<AguiMessage> effectiveMessages = new ArrayList<>(pendingClientMessages);
        if (latestUserMessage != null
                && !A2UI_ACTION_PLACEHOLDER.equals(latestUserMessage.getContent())) {
            effectiveMessages.add(latestUserMessage);
        }
        if (effectiveMessages.isEmpty()) {
            throw new IllegalArgumentException("AGUI 请求缺少用户消息");
        }
        return RunAgentInput.builder()
                .threadId(sessionId)
                .runId(input.getRunId())
                .messages(effectiveMessages)
                .tools(input.getTools())
                .context(input.getContext())
                .state(input.getState())
                .forwardedProps(input.getForwardedProps())
                .build();
    }

    private AguiMessage extractLatestUserMessage(RunAgentInput input) {
        List<AguiMessage> messages = input.getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            AguiMessage candidate = messages.get(i);
            if (candidate.isUserMessage()) {
                return candidate;
            }
        }
        return null;
    }

    private List<AguiMessage> toAguiMessages(List<Msg> messages) {
        return messages.stream().map(this::toAguiMessage).toList();
    }

    private AguiMessage toAguiMessage(Msg message) {
        return AguiMessage.userMessage(buildSyntheticMessageId(message), message.getTextContent());
    }

    private String buildSyntheticMessageId(Msg message) {
        // 把暂存的 A2UI action 重新注入为普通 AGUI user message。
        // 由于 AguiMessage 不保留 Msg.metadata，历史过滤依赖 A2UI_ACTION_MESSAGE_ID_PREFIX 识别。
        return A2uiMessageApplicationService.A2UI_ACTION_MESSAGE_ID_PREFIX + message.getId();
    }

    /**
     * 将 AGUI 事件流转换为 SSE 流。
     *
     * <p>合并一个每 30 秒发送一次注释行的心跳流，防止代理服务器（Nginx 等）因
     * idle 超时（默认 60s）在 Agent 执行耗时操作期间切断 SSE 连接。
     * 注释行以 `:` 开头，客户端收到后直接忽略，不触发任何事件回调。
     */
    private Flux<ServerSentEvent<String>> toSseFlux(Flux<RunEventStreamStore.StoredRunEvent> eventFlux) {
        // 该方法内部同时需要数据流和完成信号，必须共享同一个上游，避免对冷流产生双订阅。
        Flux<RunEventStreamStore.StoredRunEvent> sharedEventFlux = eventFlux.publish().autoConnect(2);

        // 每 30s 发送一个 SSE 注释行，保持连接活跃
        Flux<ServerSentEvent<String>> heartbeat = Flux.interval(Duration.ofSeconds(30))
                .map(i -> ServerSentEvent.<String>builder().comment("heartbeat").build());

        Flux<ServerSentEvent<String>> dataFlux = sharedEventFlux.map(event ->
                ServerSentEvent.<String>builder()
                        .id(Long.toString(event.seq()))
                        .data(event.payloadJson())
                        .build());

        // eventFlux 完成时，心跳流随之结束，SSE 连接正常关闭
        return Flux.merge(dataFlux, heartbeat.takeUntilOther(sharedEventFlux.ignoreElements()));
    }
}
