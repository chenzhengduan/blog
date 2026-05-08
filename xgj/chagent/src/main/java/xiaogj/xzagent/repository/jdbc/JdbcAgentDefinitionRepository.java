package xiaogj.xzagent.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.AgentDefinition;
import xiaogj.xzagent.repository.AgentDefinitionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 基于 JDBC 的 Agent 定义仓储实现。
 */
@Repository
public class JdbcAgentDefinitionRepository implements AgentDefinitionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcAgentDefinitionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<AgentDefinition> findActive() {
        return jdbcTemplate.query(
                """
                select agent_id, name, system_prompt, agents_md, max_iterations, skill_ids_json,
                       enabled, created_at, updated_at
                from agent_definition
                where enabled = true
                order by updated_at desc
                limit 1
                """,
                rs -> {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new AgentDefinition(
                            rs.getString("agent_id"),
                            rs.getString("name"),
                            rs.getString("system_prompt"),
                            rs.getString("agents_md"),
                            rs.getInt("max_iterations"),
                            readSkillIds(rs.getString("skill_ids_json")),
                            rs.getBoolean("enabled"),
                            toInstant(rs.getTimestamp("created_at")),
                            toInstant(rs.getTimestamp("updated_at"))));
                });
    }

    @Override
    public void upsert(AgentDefinition definition) {
        jdbcTemplate.update(
                """
                insert into agent_definition(
                    agent_id, name, system_prompt, agents_md, max_iterations, skill_ids_json,
                    enabled, created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on duplicate key update
                    name = values(name),
                    system_prompt = values(system_prompt),
                    agents_md = values(agents_md),
                    max_iterations = values(max_iterations),
                    skill_ids_json = values(skill_ids_json),
                    enabled = values(enabled),
                    updated_at = values(updated_at)
                """,
                definition.agentId(),
                definition.name(),
                definition.systemPrompt(),
                definition.agentsMd(),
                definition.maxIterations(),
                writeSkillIds(definition.skillIds()),
                definition.enabled(),
                Timestamp.from(definition.createdAt()),
                Timestamp.from(definition.updatedAt()));
    }

    /**
     * Skill 列表采用 JSON 文本持久化，避免在 V1 过早引入单独绑定表。
     *
     * <p>当前系统只支持一个默认 Agent，这种折中方式能用最小迁移成本把
     * Agent 级 Skill 选择真正落入数据库，同时为后续拆出独立绑定表预留空间。
     */
    private List<String> readSkillIds(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("无法解析 Agent Skill 列表 JSON", e);
        }
    }

    /**
     * 统一序列化 Agent 绑定的 Skill 列表，避免不同写入路径产生格式偏差。
     */
    private String writeSkillIds(List<String> skillIds) {
        try {
            return objectMapper.writeValueAsString(
                    skillIds == null ? Collections.<String>emptyList() : skillIds);
        } catch (IOException e) {
            throw new IllegalStateException("无法序列化 Agent Skill 列表 JSON", e);
        }
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }
}
