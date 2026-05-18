package xiaogj.xzagent.repository;

public interface AgentRunRepository {
    /**
     * 创建 RUNNING 状态的运行记录。
     *
     * <p>除 runId / sessionId 外，同时记录 ownerNodeId 和首个 heartbeat 时间，
     * 便于后续分布式取消、补流排障以及 owner 漂移诊断。
     */
    void createRunning(String runId, String sessionId, String ownerNodeId);

    /**
     * 在进入最终收口前先标记为 FINALIZING。
     */
    void markFinalizing(String runId, Long lastEventSeq);

    /**
     * 将运行实例标记为已完成，并记录模型的生成原因。
     */
    void markCompleted(String runId, String generateReason, Long lastEventSeq);

    /**
     * 将运行实例标记为失败，并保留可追踪的错误信息。
     */
    void markFailed(String runId, String errorMessage, Long lastEventSeq);

    /**
     * 将运行实例标记为已取消。
     */
    void markCancelled(String runId, String finishedReason, Long lastEventSeq);

    /**
     * 刷新运行心跳。
     */
    void heartbeat(String runId, Long lastEventSeq);
}
