package xiaogj.xzagent.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 基于 JDBC 的远程工具元数据源仓储实现。
 */
@Repository
public class JdbcRemoteToolSourceRepository implements RemoteToolSourceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcRemoteToolSourceRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<RemoteToolSourceDefinition> findEnabledSources() {
        return queryByEnabled(true);
    }

    @Override
    public List<RemoteToolSourceDefinition> findAll() {
        return jdbcTemplate.query(
                """
                select source_id, name, meta_url, inline_content, enabled, headers_json, timeout_seconds,
                       refresh_interval_seconds, created_at, updated_at
                from remote_tool_source
                order by source_id
                """,
                (rs, rowNum) -> new RemoteToolSourceDefinition(
                        rs.getString("source_id"),
                        rs.getString("name"),
                        rs.getString("meta_url"),
                        rs.getString("inline_content"),
                        rs.getBoolean("enabled"),
                        readStringMap(rs.getString("headers_json")),
                        Duration.ofSeconds(rs.getLong("timeout_seconds")),
                        Duration.ofSeconds(rs.getLong("refresh_interval_seconds")),
                        toInstant(rs.getTimestamp("created_at")),
                        toInstant(rs.getTimestamp("updated_at"))));
    }

    @Override
    public void upsert(RemoteToolSourceDefinition definition) {
        jdbcTemplate.update(
                """
                insert into remote_tool_source(
                    source_id, name, meta_url, inline_content, enabled, headers_json,
                    timeout_seconds, refresh_interval_seconds, created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on duplicate key update
                    name = values(name),
                    meta_url = values(meta_url),
                    inline_content = values(inline_content),
                    enabled = values(enabled),
                    headers_json = values(headers_json),
                    timeout_seconds = values(timeout_seconds),
                    refresh_interval_seconds = values(refresh_interval_seconds),
                    updated_at = values(updated_at)
                """,
                definition.sourceId(),
                definition.name(),
                definition.metaUrl(),
                definition.inlineContent(),
                definition.enabled(),
                writeStringMap(definition.headers()),
                definition.timeout().toSeconds(),
                definition.refreshInterval().toSeconds(),
                Timestamp.from(definition.createdAt()),
                Timestamp.from(definition.updatedAt()));
    }

    @Override
    public void deleteBySourceId(String sourceId) {
        jdbcTemplate.update("delete from remote_tool_source where source_id = ?", sourceId);
    }

    private List<RemoteToolSourceDefinition> queryByEnabled(boolean enabled) {
        return jdbcTemplate.query(
                """
                select source_id, name, meta_url, inline_content, enabled, headers_json, timeout_seconds,
                       refresh_interval_seconds, created_at, updated_at
                from remote_tool_source
                where enabled = ?
                order by source_id
                """,
                (rs, rowNum) -> new RemoteToolSourceDefinition(
                        rs.getString("source_id"),
                        rs.getString("name"),
                        rs.getString("meta_url"),
                        rs.getString("inline_content"),
                        rs.getBoolean("enabled"),
                        readStringMap(rs.getString("headers_json")),
                        Duration.ofSeconds(rs.getLong("timeout_seconds")),
                        Duration.ofSeconds(rs.getLong("refresh_interval_seconds")),
                        toInstant(rs.getTimestamp("created_at")),
                        toInstant(rs.getTimestamp("updated_at"))),
                enabled);
    }

    /**
     * 统一解析字符串 Map，避免每个仓储各自实现 JSON 反序列化细节。
     */
    private Map<String, String> readStringMap(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("无法解析远程工具源请求头 JSON", e);
        }
    }

    /**
     * 对 JDBC 返回的可空时间戳做统一转换，避免调用方散落空判断逻辑。
     */
    private Instant toInstant(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    /**
     * 将请求头 Map 序列化为 JSON，统一复用现有 ObjectMapper 配置。
     */
    private String writeStringMap(Map<String, String> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("无法序列化远程工具源请求头 JSON", e);
        }
    }
}
