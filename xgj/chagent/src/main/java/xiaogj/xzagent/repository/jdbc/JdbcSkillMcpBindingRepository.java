package xiaogj.xzagent.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import xiaogj.xzagent.model.SkillMcpBinding;
import xiaogj.xzagent.repository.SkillMcpBindingRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSkillMcpBindingRepository implements SkillMcpBindingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcSkillMcpBindingRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SkillMcpBinding> findEnabledBindings() {
        return jdbcTemplate.query(
                """
                select skill_id, server_id, enable_tools_json, disable_tools_json, enabled
                from skill_mcp_binding
                where enabled = true
                """,
                (rs, rowNum) -> new SkillMcpBinding(
                        rs.getString("skill_id"),
                        rs.getString("server_id"),
                        readList(rs.getString("enable_tools_json")),
                        readList(rs.getString("disable_tools_json")),
                        rs.getBoolean("enabled")));
    }

    @Override
    public List<SkillMcpBinding> findByServerId(String serverId) {
        return jdbcTemplate.query(
                """
                select skill_id, server_id, enable_tools_json, disable_tools_json, enabled
                from skill_mcp_binding
                where server_id = ?
                order by skill_id asc
                """,
                (rs, rowNum) -> new SkillMcpBinding(
                        rs.getString("skill_id"),
                        rs.getString("server_id"),
                        readList(rs.getString("enable_tools_json")),
                        readList(rs.getString("disable_tools_json")),
                        rs.getBoolean("enabled")),
                serverId);
    }

    @Override
    public List<SkillMcpBinding> findBySkillId(String skillId) {
        return jdbcTemplate.query(
                """
                select skill_id, server_id, enable_tools_json, disable_tools_json, enabled
                from skill_mcp_binding
                where skill_id = ?
                order by server_id asc
                """,
                (rs, rowNum) -> new SkillMcpBinding(
                        rs.getString("skill_id"),
                        rs.getString("server_id"),
                        readList(rs.getString("enable_tools_json")),
                        readList(rs.getString("disable_tools_json")),
                        rs.getBoolean("enabled")),
                skillId);
    }

    @Override
    public void replaceByServerId(String serverId, List<SkillMcpBinding> bindings) {
        jdbcTemplate.update("delete from skill_mcp_binding where server_id = ?", serverId);
        for (SkillMcpBinding binding : bindings) {
            jdbcTemplate.update(
                    """
                    insert into skill_mcp_binding(
                        skill_id, server_id, enable_tools_json, disable_tools_json, enabled
                    ) values (?, ?, ?, ?, ?)
                    """,
                    binding.skillId(),
                    binding.serverId(),
                    writeList(binding.enableTools()),
                    writeList(binding.disableTools()),
                    binding.enabled());
        }
    }

    @Override
    public void replaceBySkillId(String skillId, List<SkillMcpBinding> bindings) {
        jdbcTemplate.update("delete from skill_mcp_binding where skill_id = ?", skillId);
        for (SkillMcpBinding binding : bindings) {
            jdbcTemplate.update(
                    """
                    insert into skill_mcp_binding(
                        skill_id, server_id, enable_tools_json, disable_tools_json, enabled
                    ) values (?, ?, ?, ?, ?)
                    """,
                    binding.skillId(),
                    binding.serverId(),
                    writeList(binding.enableTools()),
                    writeList(binding.disableTools()),
                    binding.enabled());
        }
    }

    private List<String> readList(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("无法解析 Skill MCP 绑定 JSON", e);
        }
    }

    private String writeList(List<String> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("无法序列化 Skill MCP 绑定 JSON", e);
        }
    }
}
