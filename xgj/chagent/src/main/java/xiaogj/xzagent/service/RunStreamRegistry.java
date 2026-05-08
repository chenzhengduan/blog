package xiaogj.xzagent.service;

import java.util.Optional;
import reactor.core.publisher.Sinks;

/**
 * 活跃 Agent 运行注册表。
 *
 * <p>每个 sessionId 对应最多一个正在后台执行的 Agent 运行。
 * 注册表负责管理其生命周期，使前端意外断开后重连时可直接接入正在执行的流，
 * 也使取消接口能拿到 Agent 实例发出中断信号。
 *
 * <p>当前实现为内存版本（{@link InMemoryRunStreamRegistry}），适合单节点部署。
 * 多节点部署时，运行执行权由 Redis 协调，但 owner 节点内仍需要一个本地注册表
 * 保存运行时实例与 live 热流，供当前节点在线订阅者复用。
 */
public interface RunStreamRegistry {

    /**
     * 一次活跃 Agent 运行的上下文，同时持有热流 Sink 和运行时快照。
     *
     * @param runtime 运行时快照，含 Agent 实例，用于中断和状态收口
     * @param sink    热流 Sink，前端订阅此流接收已经编号并持久化过的运行事件；
     *                无订阅者时 live 事件可以丢弃，但 Redis Stream 中仍保留可重放副本
     */
    record ActiveRun(AgentRuntime runtime, Sinks.Many<RunEventStreamStore.StoredRunEvent> sink) {}

    /**
     * 注册一次活跃的 Agent 运行。
     * 同一 sessionId 同时只允许一个活跃运行。
     *
     * @param sessionId 会话标识
     * @param activeRun 运行上下文
     */
    void register(String sessionId, ActiveRun activeRun);

    /**
     * 查找指定 session 是否有正在执行的 Agent 运行。
     *
     * @param sessionId 会话标识
     * @return 若存在则返回运行上下文，否则返回 empty
     */
    Optional<ActiveRun> find(String sessionId);

    /**
     * 移除已完成、已失败或已取消的运行。
     * 移除后，下一次 /agui/run 请求将视为全新运行。
     *
     * @param sessionId 会话标识
     */
    void remove(String sessionId);
}
