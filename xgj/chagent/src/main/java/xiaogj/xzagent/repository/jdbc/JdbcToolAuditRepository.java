package xiaogj.xzagent.repository.jdbc;

import xiaogj.xzagent.repository.ToolAuditRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcToolAuditRepository implements ToolAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcToolAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAudit(
            String runId, String toolName, String toolArgsJson, String toolResultJson, String status) {
        jdbcTemplate.update(
                """
                insert into agent_tool_audit(run_id, tool_name, tool_args_json, tool_result_json, status, created_at)
                values (?, ?, ?, ?, ?, current_timestamp)
                """,
                runId,
                toolName,
                toolArgsJson,
                toolResultJson,
                status);
    }
}
