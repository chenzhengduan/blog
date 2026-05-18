package xiaogj.xzagent.model;

/**
 * Agent 运行状态。
 *
 * <p>该状态既用于 MySQL 中的 {@code agent_run.status}，也用于 Redis 中的
 * 运行中元数据表达。保持统一枚举后，后续 controller、协调层和历史查询层
 * 可以共享同一套状态机语义。
 */
public enum AgentRunStatus {
    RUNNING,
    FINALIZING,
    COMPLETED,
    FAILED,
    CANCELLED
}
