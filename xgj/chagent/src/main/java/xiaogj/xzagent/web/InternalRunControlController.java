package xiaogj.xzagent.web;

import java.util.Optional;
import xiaogj.xzagent.model.DistributedRunMetadata;
import xiaogj.xzagent.service.AgentRunFinalizationService;
import xiaogj.xzagent.service.AgentRuntime;
import xiaogj.xzagent.service.DistributedRunCoordinator;
import xiaogj.xzagent.service.RunEventStreamStore;
import xiaogj.xzagent.service.RunHeartbeatService;
import xiaogj.xzagent.service.RunStreamRegistry;
import xiaogj.xzagent.web.dto.AguiCancelResponse;
import io.agentscope.core.agui.event.AguiEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集群内部运行控制接口。
 *
 * <p>该控制器只面向节点间直接调用，不供前端或网关公开使用。
 * 路径默认带 {@code /private} 前缀，利用当前项目网关约定把接口留在集群内。
 */
@RestController
@RequestMapping("${xzagent.distributed-run.internal-control-path-prefix:/xzagent/private/internal}")
public class InternalRunControlController {

    private static final Logger log = LoggerFactory.getLogger(InternalRunControlController.class);

    private final DistributedRunCoordinator distributedRunCoordinator;
    private final RunStreamRegistry runStreamRegistry;
    private final RunEventStreamStore runEventStreamStore;
    private final RunHeartbeatService runHeartbeatService;
    private final AgentRunFinalizationService agentRunFinalizationService;

    public InternalRunControlController(
            DistributedRunCoordinator distributedRunCoordinator,
            RunStreamRegistry runStreamRegistry,
            RunEventStreamStore runEventStreamStore,
            RunHeartbeatService runHeartbeatService,
            AgentRunFinalizationService agentRunFinalizationService) {
        this.distributedRunCoordinator = distributedRunCoordinator;
        this.runStreamRegistry = runStreamRegistry;
        this.runEventStreamStore = runEventStreamStore;
        this.runHeartbeatService = runHeartbeatService;
        this.agentRunFinalizationService = agentRunFinalizationService;
    }

    /**
     * 由其他节点转发过来的取消请求。
     */
    @PostMapping("/runs/{runId}/cancel")
    public ResponseEntity<AguiCancelResponse> cancelRun(@PathVariable String runId) {
        Optional<DistributedRunMetadata> metadataOptional = distributedRunCoordinator.findRun(runId);
        if (metadataOptional.isEmpty()) {
            return ResponseEntity.ok(AguiCancelResponse.noActiveRun());
        }
        DistributedRunMetadata metadata = metadataOptional.get();
        Optional<RunStreamRegistry.ActiveRun> activeRun = runStreamRegistry.find(metadata.sessionId());
        if (activeRun.isEmpty() || !runId.equals(activeRun.get().runtime().runId())) {
            return ResponseEntity.ok(AguiCancelResponse.noActiveRun());
        }

        RunStreamRegistry.ActiveRun run = activeRun.get();
        AgentRuntime runtime = run.runtime();
        log.info("收到内部取消请求: sessionId={}, runId={}", metadata.sessionId(), runId);
        runtime.agent().interrupt();
        distributedRunCoordinator.requestCancel(runId);
        distributedRunCoordinator.updateStatus(runId, xiaogj.xzagent.model.AgentRunStatus.FINALIZING);
        runHeartbeatService.stop(runId);
        runStreamRegistry.remove(metadata.sessionId());
        long finishedSeq = metadata.lastEventSeq() + 1;
        RunEventStreamStore.StoredRunEvent finishedEvent = runEventStreamStore.append(
                runId,
                finishedSeq,
                new AguiEvent.RunFinished(metadata.sessionId(), runId));
        distributedRunCoordinator.updateLastEventSeq(runId, finishedSeq);
        run.sink().tryEmitNext(finishedEvent);
        distributedRunCoordinator.updateStatus(runId, xiaogj.xzagent.model.AgentRunStatus.CANCELLED);
        run.sink().tryEmitComplete();
        runEventStreamStore.expire(runId);
        try {
            agentRunFinalizationService.cancelAgui(runtime, runId, finishedSeq);
        } finally {
            distributedRunCoordinator.clear(metadata.sessionId(), runId);
        }
        return ResponseEntity.ok(AguiCancelResponse.ok());
    }
}
