package xiaogj.xzagent.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import xiaogj.xzagent.model.McpServerDefinition;
import xiaogj.xzagent.model.McpTransportType;
import xiaogj.xzagent.repository.McpServerRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMcpServerRepository implements McpServerRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcMcpServerRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<McpServerDefinition> findEnabledServers() {
        return query(enabledClause(true), true);
    }

    @Override
    public List<McpServerDefinition> findAll() {
        return query("", null);
    }

    @Override
    public void upsert(McpServerDefinition definition) {
        jdbcTemplate.update(
                """
                insert into mcp_server(
                    server_id, name, transport_type, enabled, config_json, headers_json,
                    query_params_json, timeout_seconds, initialization_timeout_seconds
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on duplicate key update
                    name = values(name),
                    transport_type = values(transport_type),
                    enabled = values(enabled),
                    config_json = values(config_json),
                    headers_json = values(headers_json),
                    query_params_json = values(query_params_json),
                    timeout_seconds = values(timeout_seconds),
                    initialization_timeout_seconds = values(initialization_timeout_seconds)
                """,
                definition.serverId(),
                definition.name(),
                definition.transportType().name(),
                definition.enabled(),
                writeJsonMap(definition.config()),
                writeStringMap(definition.headers()),
                writeStringMap(definition.queryParams()),
                definition.timeout().toSeconds(),
                definition.initializationTimeout().toSeconds());
    }

    private List<McpServerDefinition> query(String whereClause, Boolean enabled) {
        return jdbcTemplate.query(
                """
                select server_id, name, transport_type, enabled, config_json, headers_json, query_params_json,
                       timeout_seconds, initialization_timeout_seconds
                from mcp_server
                """ + whereClause,
                (rs, rowNum) -> new McpServerDefinition(
                        rs.getString("server_id"),
                        rs.getString("name"),
                        McpTransportType.valueOf(rs.getString("transport_type")),
                        rs.getBoolean("enabled"),
                        readJsonMap(rs.getString("config_json")),
                        readStringMap(rs.getString("headers_json")),
                        readStringMap(rs.getString("query_params_json")),
                        Duration.ofSeconds(rs.getLong("timeout_seconds")),
                        Duration.ofSeconds(rs.getLong("initialization_timeout_seconds"))),
                enabled == null ? new Object[]{} : new Object[]{enabled});
    }

    private String enabledClause(boolean enabled) {
        return " where enabled = ? ";
    }

    private Map<String, Object> readJsonMap(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("无法解析 MCP 配置 JSON", e);
        }
    }

    private Map<String, String> readStringMap(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("无法解析 MCP 字符串 JSON", e);
        }
    }

    private String writeJsonMap(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("无法序列化 MCP 配置 JSON", e);
        }
    }

    private String writeStringMap(Map<String, String> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("无法序列化 MCP 字符串 JSON", e);
        }
    }
}
