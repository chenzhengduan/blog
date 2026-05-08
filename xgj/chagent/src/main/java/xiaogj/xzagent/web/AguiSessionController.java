package xiaogj.xzagent.web;

import io.agentscope.core.agui.encoder.AguiEventEncoder;
import io.agentscope.core.agui.event.AguiEvent;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import xiaogj.xzagent.config.XzagentDistributedRunProperties;
import xiaogj.xzagent.model.DistributedRunMetadata;
import xiaogj.xzagent.service.AgentRunFinalizationService;
import xiaogj.xzagent.service.AgentRuntime;
import xiaogj.xzagent.service.DistributedRunCoordinator;
import xiaogj.xzagent.service.NodeIdentityProvider;
import xiaogj.xzagent.service.RunEventStreamStore;
import xiaogj.xzagent.service.RunHeartbeatService;
import xiaogj.xzagent.service.RunStreamRegistry;
import xiaogj.xzagent.service.UserPrincipal;
import xiaogj.xzagent.util.SessionIdValidator;
import xiaogj.xzagent.web.dto.AguiCancelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * 管理 AGUI session 级别的操作：重连流式输出、主动取消 Agent 运行。
 *
 * <p>接口统一挂载在 /xzagent/api/agui/sessions/** 下，符合 nginx 反向代理的 /api/ 路由规则。
 * 而 /xzagent/agui/run 保留在 {@link AguiChatController}，因为该路径有独立的 nginx 代理配置。
 */
@RestController
@RequestMapping("/xzagent/api/agui/sessions")
public class AguiSessionController {

    private static final Logger log = LoggerFactory.getLogger(AguiSessionController.class);

    private final RunStreamRegistry runStreamRegistry;
    private final DistributedRunCoordinator distributedRunCoordinator;
    private final RunEventStreamStore runEventStreamStore;
    private final RunHeartbeatService runHeartbeatService;
    private final NodeIdentityProvider nodeIdentityProvider;
    private final XzagentDistributedRunProperties distributedRunProperties;
    private final WebClient.Builder webClientBuilder;
    private final AgentRunFinalizationService agentRunFinalizationService;
    private final AguiEventEncoder aguiEventEncoder = new AguiEventEncoder();

    public AguiSessionController(
            RunStreamRegistry runStreamRegistry,
            DistributedRunCoordinator distributedRunCoordinator,
            RunEventStreamStore runEventStreamStore,
            RunHeartbeatService runHeartbeatService,
            NodeIdentityProvider nodeIdentityProvider,
            XzagentDistributedRunProperties distributedRunProperties,
            WebClient.Builder webClientBuilder,
            AgentRunFinalizationService agentRunFinalizationService) {
        this.runStreamRegistry = runStreamRegistry;
        this.distributedRunCoordinator = distributedRunCoordinator;
        this.runEventStreamStore = runEventStreamStore;
        this.runHeartbeatService = runHeartbeatService;
        this.nodeIdentityProvider = nodeIdentityProvider;
        this.distributedRunProperties = distributedRunProperties;
        this.webClientBuilder = webClientBuilder;
        this.agentRunFinalizationService = agentRunFinalizationService;
    }

    /**
     * 前端重连：订阅当前 session 正在执行的 Agent 流式输出。
     *
     * <p>始终返回 HTTP 200 + SSE 流，不使用 HTTP 错误码：
     * <ul>
     *     <li>若有活跃运行（Agent 仍在后台执行）：直接订阅热流，恢复流式输出。</li>
     *     <li>若无活跃运行：立即发送信号事件 {@code {"noActiveRun": true}} 后关闭流，
     *         前端收到该信号后按正常已完成状态处理。</li>
     * </ul>
     */
    @GetMapping(path = "/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> reconnectStream(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String sessionId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        String validatedSessionId = SessionIdValidator.validate(sessionId);
        Optional<DistributedRunMetadata> activeRun = distributedRunCoordinator.findActiveRun(validatedSessionId);
        if (activeRun.isEmpty()) {
            log.info("重连请求：session 无活跃运行，返回信号事件: username={}, sessionId={}",
                    principal.username(), validatedSessionId);
            AguiEvent signal = new AguiEvent.Raw(validatedSessionId, "", Map.of("noActiveRun", true));
            return Flux.just(ServerSentEvent.<String>builder()
                    .data(aguiEventEncoder.encodeToJson(signal).trim())
                    .build());
        }

        DistributedRunMetadata metadata = activeRun.get();
        long lastEventSeq = parseLastEventSeq(lastEventId);

        log.info("前端重连运行流: username={}, sessionId={}, runId={}, ownerNodeId={}, lastEventSeq={}",
                principal.username(), validatedSessionId, metadata.runId(), metadata.ownerNodeId(), lastEventSeq);
        return toSseFlux(replayThenTail(metadata, lastEventSeq));
    }

    /**
     * 主动取消正在执行的 Agent 运行。
     *
     * <p>用户点击"停止"按钮时调用。Agent 会在下一个检查点真正中断，
     * 取消后热流发送 RunFinished 事件通知仍在线的前端，然后关闭流。
     *
     * <p>始终返回 HTTP 200，通过响应体的 {@code code} 字段区分结果：
     * <ul>
     *     <li>{@code OK}：取消成功</li>
     *     <li>{@code NO_ACTIVE_RUN}：该 session 当前无活跃运行（幂等，不视为错误）</li>
     * </ul>
     */
    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<AguiCancelResponse> cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String sessionId) {
        String validatedSessionId = SessionIdValidator.validate(sessionId);
        Optional<DistributedRunMetadata> activeRun = distributedRunCoordinator.findActiveRun(validatedSessionId);
        if (activeRun.isEmpty()) {
            log.info("取消请求：session 无活跃运行: username={}, sessionId={}",
                    principal.username(), validatedSessionId);
            return ResponseEntity.ok(AguiCancelResponse.noActiveRun());
        }

        DistributedRunMetadata metadata = activeRun.get();
        log.info("用户主动取消 Agent 运行: username={}, sessionId={}, runId={}, ownerNodeId={}",
                principal.username(), validatedSessionId, metadata.runId(), metadata.ownerNodeId());

        if (!nodeIdentityProvider.currentNodeId().equals(metadata.ownerNodeId())) {
            return ResponseEntity.ok(forwardCancelToOwner(metadata));
        }

        Optional<RunStreamRegistry.ActiveRun> localRun = runStreamRegistry.find(validatedSessionId);
        if (localRun.isEmpty() || !metadata.runId().equals(localRun.get().runtime().runId())) {
            return ResponseEntity.ok(AguiCancelResponse.noActiveRun());
        }

        RunStreamRegistry.ActiveRun run = localRun.get();
        AgentRuntime runtime = run.runtime();
        runtime.agent().interrupt();
        distributedRunCoordinator.requestCancel(metadata.runId());
        distributedRunCoordinator.updateStatus(metadata.runId(), xiaogj.xzagent.model.AgentRunStatus.FINALIZING);
        runHeartbeatService.stop(metadata.runId());
        runStreamRegistry.remove(validatedSessionId);
        long finishedSeq = metadata.lastEventSeq() + 1;
        RunEventStreamStore.StoredRunEvent finishedEvent = runEventStreamStore.append(
                metadata.runId(),
                finishedSeq,
                new AguiEvent.RunFinished(validatedSessionId, metadata.runId()));
        distributedRunCoordinator.updateLastEventSeq(metadata.runId(), finishedSeq);
        run.sink().tryEmitNext(finishedEvent);
        distributedRunCoordinator.updateStatus(metadata.runId(), xiaogj.xzagent.model.AgentRunStatus.CANCELLED);
        run.sink().tryEmitComplete();
        runEventStreamStore.expire(metadata.runId());
        try {
            agentRunFinalizationService.cancelAgui(runtime, metadata.runId(), finishedSeq);
        } finally {
            distributedRunCoordinator.clear(validatedSessionId, metadata.runId());
        }

        return ResponseEntity.ok(AguiCancelResponse.ok());
    }

    private Flux<RunEventStreamStore.StoredRunEvent> replayThenTail(DistributedRunMetadata metadata, long lastEventSeq) {
        AtomicLong cursor = new AtomicLong(lastEventSeq);
        return Flux.defer(() -> {
            List<RunEventStreamStore.StoredRunEvent> initialBatch = loadReplayBatch(metadata.runId(), cursor);
            if (containsRunFinished(initialBatch)) {
                return Flux.fromIterable(initialBatch);
            }
            return Flux.fromIterable(initialBatch).concatWith(pollTailFlux(metadata.runId(), cursor));
        });
    }

    private Flux<RunEventStreamStore.StoredRunEvent> pollTailFlux(String runId, AtomicLong cursor) {
        return Flux.create(sink -> {
            reactor.core.Disposable pollingTask = Flux.interval(distributedRunProperties.livePollTimeoutDuration())
                    .subscribe(tick -> {
                        List<RunEventStreamStore.StoredRunEvent> batch = loadReplayBatch(runId, cursor);
                        if (!batch.isEmpty()) {
                            for (RunEventStreamStore.StoredRunEvent event : batch) {
                                sink.next(event);
                                if (isRunFinishedEvent(event)) {
                                    sink.complete();
                                    return;
                                }
                            }
                            return;
                        }
                        Optional<DistributedRunMetadata> latestMetadataOptional = distributedRunCoordinator.findRun(runId);
                        if (latestMetadataOptional.isEmpty()) {
                            sink.complete();
                            return;
                        }
                        DistributedRunMetadata latestMetadata = latestMetadataOptional.get();
                        if (isTerminalStatus(latestMetadata) && latestMetadata.lastEventSeq() <= cursor.get()) {
                            sink.complete();
                            return;
                        }
                        if (latestMetadata.lastEventSeq() <= cursor.get()) {
                            return;
                        }
                        List<RunEventStreamStore.StoredRunEvent> latestBatch = loadReplayBatch(runId, cursor);
                        for (RunEventStreamStore.StoredRunEvent event : latestBatch) {
                            sink.next(event);
                            if (isRunFinishedEvent(event)) {
                                sink.complete();
                                return;
                            }
                        }
                    }, sink::error);
            sink.onDispose(pollingTask::dispose);
        });
    }

    private List<RunEventStreamStore.StoredRunEvent> loadReplayBatch(String runId, AtomicLong cursor) {
        List<RunEventStreamStore.StoredRunEvent> batch = runEventStreamStore.replayAfter(
                runId,
                cursor.get(),
                distributedRunProperties.replayBatchSize());
        if (!batch.isEmpty()) {
            cursor.set(batch.get(batch.size() - 1).seq());
        }
        return batch;
    }

    private boolean containsRunFinished(List<RunEventStreamStore.StoredRunEvent> batch) {
        return batch.stream().anyMatch(this::isRunFinishedEvent);
    }

    private boolean isRunFinishedEvent(RunEventStreamStore.StoredRunEvent event) {
        return event.payloadJson().contains("\"type\":\"RUN_FINISHED\"");
    }

    private boolean isTerminalStatus(DistributedRunMetadata metadata) {
        return metadata.status() == xiaogj.xzagent.model.AgentRunStatus.COMPLETED
                || metadata.status() == xiaogj.xzagent.model.AgentRunStatus.FAILED
                || metadata.status() == xiaogj.xzagent.model.AgentRunStatus.CANCELLED;
    }

    private Flux<ServerSentEvent<String>> toSseFlux(Flux<RunEventStreamStore.StoredRunEvent> eventFlux) {
        Flux<RunEventStreamStore.StoredRunEvent> sharedEventFlux = eventFlux.publish().autoConnect(2);
        Flux<ServerSentEvent<String>> heartbeat = Flux.interval(Duration.ofSeconds(30))
                .map(i -> ServerSentEvent.<String>builder().comment("heartbeat").build());
        Flux<ServerSentEvent<String>> dataFlux = sharedEventFlux.map(event -> ServerSentEvent.<String>builder()
                .id(Long.toString(event.seq()))
                .data(event.payloadJson())
                .build());
        return Flux.merge(dataFlux, heartbeat.takeUntilOther(sharedEventFlux.ignoreElements()));
    }

    private long parseLastEventSeq(String lastEventId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(lastEventId.trim());
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private AguiCancelResponse forwardCancelToOwner(DistributedRunMetadata metadata) {
        String url = metadata.ownerBaseUrl()
                + nodeIdentityProvider.internalControlPathPrefix()
                + "/runs/"
                + metadata.runId()
                + "/cancel";
        return webClientBuilder.clone()
                .build()
                .post()
                .uri(url)
                .retrieve()
                .bodyToMono(AguiCancelResponse.class)
                .blockOptional()
                .orElseGet(AguiCancelResponse::noActiveRun);
    }
}
