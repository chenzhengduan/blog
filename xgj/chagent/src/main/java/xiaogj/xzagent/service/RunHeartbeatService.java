package xiaogj.xzagent.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import xiaogj.xzagent.config.XzagentDistributedRunProperties;
import xiaogj.xzagent.repository.AgentRunRepository;

/**
 * 运行 heartbeat 管理服务。
 *
 * <p>owner 节点在 run 执行期间周期性刷新两份状态：
 * <ul>
 *     <li>Redis 协调层 lease / heartbeat</li>
 *     <li>MySQL {@code agent_run.last_heartbeat_at / last_event_seq}</li>
 * </ul>
 *
 * <p>服务本身不关心事件是如何产生的，只通过 {@link LongSupplier} 读取
 * 当前最新事件序号，保持控制面与数据面低耦合。
 */
@Service
public class RunHeartbeatService {

    private static final Logger log = LoggerFactory.getLogger(RunHeartbeatService.class);

    /** 分布式运行配置。 */
    private final XzagentDistributedRunProperties properties;
    /** Redis 协调器。 */
    private final DistributedRunCoordinator distributedRunCoordinator;
    /** MySQL 运行记录仓储。 */
    private final AgentRunRepository agentRunRepository;
    /** 正在运行中的 heartbeat 任务。 */
    private final Map<String, Disposable> runningHeartbeats = new ConcurrentHashMap<>();

    public RunHeartbeatService(
            XzagentDistributedRunProperties properties,
            DistributedRunCoordinator distributedRunCoordinator,
            AgentRunRepository agentRunRepository) {
        this.properties = properties;
        this.distributedRunCoordinator = distributedRunCoordinator;
        this.agentRunRepository = agentRunRepository;
    }

    /**
     * 为指定 run 启动 heartbeat。
     */
    public void start(String runId, LongSupplier lastEventSeqSupplier) {
        stop(runId);
        Disposable disposable = Flux.interval(
                        properties.heartbeatIntervalDuration(),
                        properties.heartbeatIntervalDuration())
                .publishOn(Schedulers.boundedElastic())
                .subscribe(tick -> doHeartbeat(runId, lastEventSeqSupplier));
        runningHeartbeats.put(runId, disposable);
    }

    /**
     * 停止指定 run 的 heartbeat。
     */
    public void stop(String runId) {
        Disposable disposable = runningHeartbeats.remove(runId);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void doHeartbeat(String runId, LongSupplier lastEventSeqSupplier) {
        long lastEventSeq = lastEventSeqSupplier.getAsLong();
        boolean active = distributedRunCoordinator.heartbeat(runId, lastEventSeq);
        if (!active) {
            log.warn("heartbeat 续租失败，停止本地任务: runId={}, lastEventSeq={}", runId, lastEventSeq);
            stop(runId);
            return;
        }
        agentRunRepository.heartbeat(runId, lastEventSeq);
    }
}
