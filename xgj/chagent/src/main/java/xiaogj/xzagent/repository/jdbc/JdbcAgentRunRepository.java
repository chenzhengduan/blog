package xiaogj.xzagent.repository.jdbc;

import xiaogj.xzagent.model.AgentRunStatus;
import xiaogj.xzagent.repository.AgentRunRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAgentRunRepository implements AgentRunRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAgentRunRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createRunning(String runId, String sessionId, String ownerNodeId) {
        jdbcTemplate.update(
                """
                insert into agent_run(
                    run_id,
                    session_id,
                    status,
                    owner_node_id,
                    last_event_seq,
                    last_heartbeat_at,
                    started_at)
                values (?, ?, ?, ?, 0, current_timestamp, current_timestamp)
                """,
                runId,
                sessionId,
                AgentRunStatus.RUNNING.name(),
                ownerNodeId);
    }

    @Override
    public void markFinalizing(String runId, Long lastEventSeq) {
        jdbcTemplate.update(
                """
                update agent_run
                set status = ?,
                    last_event_seq = coalesce(?, last_event_seq),
                    last_heartbeat_at = current_timestamp
                where run_id = ?
                """,
                AgentRunStatus.FINALIZING.name(),
                lastEventSeq,
                runId);
    }

    @Override
    public void markCompleted(String runId, String generateReason, Long lastEventSeq) {
        jdbcTemplate.update(
                """
                update agent_run
                set status = ?,
                    generate_reason = ?,
                    finished_reason = 'completed',
                    last_event_seq = coalesce(?, last_event_seq),
                    last_heartbeat_at = current_timestamp,
                    finished_at = current_timestamp
                where run_id = ?
                """,
                AgentRunStatus.COMPLETED.name(),
                generateReason,
                lastEventSeq,
                runId);
    }

    @Override
    public void markFailed(String runId, String errorMessage, Long lastEventSeq) {
        jdbcTemplate.update(
                """
                update agent_run
                set status = ?,
                    error_message = ?,
                    finished_reason = 'failed',
                    last_event_seq = coalesce(?, last_event_seq),
                    last_heartbeat_at = current_timestamp,
                    finished_at = current_timestamp
                where run_id = ?
                """,
                AgentRunStatus.FAILED.name(),
                errorMessage,
                lastEventSeq,
                runId);
    }

    @Override
    public void markCancelled(String runId, String finishedReason, Long lastEventSeq) {
        jdbcTemplate.update(
                """
                update agent_run
                set status = ?,
                    error_message = null,
                    finished_reason = ?,
                    last_event_seq = coalesce(?, last_event_seq),
                    last_heartbeat_at = current_timestamp,
                    finished_at = current_timestamp
                where run_id = ?
                """,
                AgentRunStatus.CANCELLED.name(),
                finishedReason,
                lastEventSeq,
                runId);
    }

    @Override
    public void heartbeat(String runId, Long lastEventSeq) {
        jdbcTemplate.update(
                """
                update agent_run
                set last_event_seq = coalesce(?, last_event_seq),
                    last_heartbeat_at = current_timestamp
                where run_id = ?
                """,
                lastEventSeq,
                runId);
    }
}
