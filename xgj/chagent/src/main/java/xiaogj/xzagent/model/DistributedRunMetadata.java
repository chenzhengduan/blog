package xiaogj.xzagent.model;

import java.time.Instant;

/**
 * 分布式活跃运行元数据。
 *
 * <p>这份模型描述的是“运行中协调层的事实”，主要来自 Redis；
 * 它不是会话历史读模型，也不是 {@code agent_run} 的完整投影。
 * 因此字段刻意保持最小集合，只保留 owner 定位、心跳、取消与补流必需的信息。
 *
 * @param sessionId 会话标识
 * @param runId 本次运行标识
 * @param ownerNodeId 当前拥有执行权的节点标识
 * @param ownerBaseUrl owner 节点对集群内暴露的内部基地址
 * @param status 当前运行状态
 * @param lastEventSeq 已落入事件流的最后一个递增序号
 * @param startedAt 运行开始时间
 * @param lastHeartbeatAt 最近一次 heartbeat 时间
 * @param cancelRequested 是否已收到取消请求
 */
public record DistributedRunMetadata(
        String sessionId,
        String runId,
        String ownerNodeId,
        String ownerBaseUrl,
        AgentRunStatus status,
        long lastEventSeq,
        Instant startedAt,
        Instant lastHeartbeatAt,
        boolean cancelRequested) {
}
