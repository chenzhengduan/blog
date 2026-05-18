package xiaogj.xzagent.repository.jdbc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import xiaogj.xzagent.repository.SessionActiveSkillRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSessionActiveSkillRepository implements SessionActiveSkillRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSessionActiveSkillRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<String> findActiveSkillIds(String sessionId) {
        return jdbcTemplate.queryForList(
                """
                select skill_id
                from session_active_skill
                where session_id = ?
                order by skill_id
                """,
                String.class,
                sessionId);
    }

    @Override
    public void replaceActiveSkills(String sessionId, List<String> skillIds) {
        // 采用全量替换而不是差量更新，简化一致性维护，
        // 也便于和 SkillBox 当前真实状态保持同步。
        jdbcTemplate.update("delete from session_active_skill where session_id = ?", sessionId);
        if (skillIds.isEmpty()) {
            return;
        }
        Timestamp activatedAt = Timestamp.from(Instant.now());
        jdbcTemplate.batchUpdate(
                """
                insert into session_active_skill(session_id, skill_id, activated_at)
                values (?, ?, ?)
                """,
                skillIds,
                skillIds.size(),
                (ps, skillId) -> {
                    ps.setString(1, sessionId);
                    ps.setString(2, skillId);
                    ps.setTimestamp(3, activatedAt);
                });
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        jdbcTemplate.update("delete from session_active_skill where session_id = ?", sessionId);
    }
}
