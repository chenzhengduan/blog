package xiaogj.xzagent.repository.jdbc;

import xiaogj.xzagent.model.SkillLocalToolBinding;
import xiaogj.xzagent.repository.SkillLocalToolBindingRepository;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSkillLocalToolBindingRepository implements SkillLocalToolBindingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSkillLocalToolBindingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SkillLocalToolBinding> findEnabledBindings() {
        return jdbcTemplate.query(
                """
                select skill_id, tool_name, enabled
                from skill_local_tool_binding
                where enabled = true
                """,
                (rs, rowNum) -> new SkillLocalToolBinding(
                        rs.getString("skill_id"),
                        rs.getString("tool_name"),
                        rs.getBoolean("enabled")));
    }

    @Override
    public void replaceBySkillId(String skillId, List<SkillLocalToolBinding> bindings) {
        jdbcTemplate.update("delete from skill_local_tool_binding where skill_id = ?", skillId);
        for (SkillLocalToolBinding binding : bindings) {
            jdbcTemplate.update(
                    """
                    insert into skill_local_tool_binding(skill_id, tool_name, enabled)
                    values (?, ?, ?)
                    """,
                    binding.skillId(),
                    binding.toolName(),
                    binding.enabled());
        }
    }
}
