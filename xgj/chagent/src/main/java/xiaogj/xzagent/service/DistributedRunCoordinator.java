package xiaogj.xzagent.service;

import java.util.Optional;
import xiaogj.xzagent.model.DistributedRunMetadata;

/**
 * 分布式活跃运行协调器。
 *
 * <p>该接口只负责“运行中”的 owner 定位、lease 续租和取消标记，
 * 不负责会话历史持久化，也不直接执行 Agent。这样后续若把 Redis 替换为
 * 其他协调组件，controller 和运行链路无需感知底层实现差异。
 */
public interface DistributedRunCoordinator {

    /**
     * 尝试抢占某个 session 的活跃执行权。
     *
     * @return true 表示当前节点成功成为 owner；false 表示已有其他 owner
     */
    boolean tryClaim(String sessionId, String runId, String ownerNodeId, String ownerBaseUrl);

    /**
     * 根据 sessionId 查询当前活跃 run。
     */
    Optional<DistributedRunMetadata> findActiveRun(String sessionId);

    /**
     * 根据 runId 查询运行元数据。
     */
    Optional<DistributedRunMetadata> findRun(String runId);

    /**
     * 刷新 owner 节点 heartbeat，并同步最近已落盘的事件序号。
     *
     * @return true 表示 run 仍然由当前协调层视为活跃；false 表示 lease 已丢失
     */
    boolean heartbeat(String runId, long lastEventSeq);

    /**
     * 仅更新最后事件序号，不刷新 lease。
     */
    void updateLastEventSeq(String runId, long lastEventSeq);

    /**
     * 更新运行状态元数据。
     */
    void updateStatus(String runId, xiaogj.xzagent.model.AgentRunStatus status);

    /**
     * 标记该 run 已收到取消请求。
     */
    void requestCancel(String runId);

    /**
     * 释放 session → run 的活跃绑定，并清理短生命周期协调键。
     */
    void clear(String sessionId, String runId);
}
