package xiaogj.xzagent.repository;

public interface ToolAuditRepository {
    /**
     * 持久化一次工具调用审计记录。
     */
    void saveAudit(String runId, String toolName, String toolArgsJson, String toolResultJson, String status);
}
