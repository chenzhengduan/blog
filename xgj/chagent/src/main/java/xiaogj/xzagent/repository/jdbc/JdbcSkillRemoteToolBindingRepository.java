package xiaogj.xzagent.repository.jdbc;

import java.util.List;
import xiaogj.xzagent.model.SkillRemoteToolBinding;
import xiaogj.xzagent.repository.SkillRemoteToolBindingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 基于 JDBC 的 Skill 远程工具绑定仓储实现。
 */
@Repository
public class JdbcSkillRemoteToolBindingRepository implements SkillRemoteToolBindingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSkillRemoteToolBindingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SkillRemoteToolBinding> findEnabledBindings() {
        return jdbcTemplate.query(
                """
                select skill_id, source_id, tool_name, enabled
                from skill_remote_tool_binding
                where enabled = true
                order by skill_id, source_id, tool_name
                """,
                (rs, rowNum) -> new SkillRemoteToolBinding(
                        rs.getString("skill_id"),
                        rs.getString("source_id"),
                        rs.getString("tool_name"),
                        rs.getBoolean("enabled")));
    }

    @Override
    public List<SkillRemoteToolBinding> findEnabledBindingsBySkillId(String skillId) {
        return jdbcTemplate.query(
                """
                select skill_id, source_id, tool_name, enabled
                from skill_remote_tool_binding
                where enabled = true
                  and skill_id = ?
                order by source_id, tool_name
                """,
                (rs, rowNum) -> new SkillRemoteToolBinding(
                        rs.getString("skill_id"),
                        rs.getString("source_id"),
                        rs.getString("tool_name"),
                        rs.getBoolean("enabled")),
                skillId);
    }

    @Override
    public void replaceBySourceId(String sourceId, List<SkillRemoteToolBinding> bindings) {
        deleteBySourceId(sourceId);
        for (SkillRemoteToolBinding binding : bindings) {
            jdbcTemplate.update(
                    """
                    insert into skill_remote_tool_binding(skill_id, source_id, tool_name, enabled)
                    values (?, ?, ?, ?)
                    """,
                    binding.skillId(),
                    binding.sourceId(),
                    binding.toolName(),
                    binding.enabled());
        }
    }

    @Override
    public void deleteBySourceId(String sourceId) {
        jdbcTemplate.update("delete from skill_remote_tool_binding where source_id = ?", sourceId);
    }

    @Override
    public void replaceBySkillId(String skillId, List<SkillRemoteToolBinding> bindings) {
        jdbcTemplate.update("delete from skill_remote_tool_binding where skill_id = ?", skillId);
        for (SkillRemoteToolBinding binding : bindings) {
            jdbcTemplate.update(
                    """
                    insert into skill_remote_tool_binding(skill_id, source_id, tool_name, enabled)
                    values (?, ?, ?, ?)
                    """,
                    binding.skillId(),
                    binding.sourceId(),
                    binding.toolName(),
                    binding.enabled());
        }
    }
}
