package xiaogj.xzagent.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import xiaogj.xzagent.model.A2uiSurfaceState;
import xiaogj.xzagent.repository.A2uiSurfaceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcA2uiSurfaceRepository implements A2uiSurfaceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcA2uiSurfaceRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<A2uiSurfaceState> findBySessionId(String sessionId) {
        return jdbcTemplate.query(
                """
                select surface_id, catalog_id, components_json, data_model_json, last_updated_msg_id
                from session_a2ui_surface
                where session_id = ?
                order by updated_at asc, surface_id asc
                """,
                (rs, rowNum) -> new A2uiSurfaceState(
                        rs.getString("surface_id"),
                        rs.getString("catalog_id"),
                        readComponents(rs.getString("components_json")),
                        readDataModel(rs.getString("data_model_json")),
                        rs.getString("last_updated_msg_id")),
                sessionId);
    }

    @Override
    public List<A2uiSurfaceState> findBySessionIdAndLastUpdatedMsgIds(String sessionId, Collection<String> messageIds) {
        List<String> ids = sanitizeIds(messageIds);
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = """
                select surface_id, catalog_id, components_json, data_model_json, last_updated_msg_id
                from session_a2ui_surface
                where session_id = ?
                  and last_updated_msg_id in (%s)
                order by updated_at asc, surface_id asc
                """.formatted(placeholders);
        List<Object> args = new ArrayList<>(ids.size() + 1);
        args.add(sessionId);
        args.addAll(ids);
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new A2uiSurfaceState(
                        rs.getString("surface_id"),
                        rs.getString("catalog_id"),
                        readComponents(rs.getString("components_json")),
                        readDataModel(rs.getString("data_model_json")),
                        rs.getString("last_updated_msg_id")),
                args.toArray());
    }

    @Override
    public Set<String> findBoundMessageIds(String sessionId) {
        return new LinkedHashSet<>(jdbcTemplate.queryForList(
                """
                select distinct last_updated_msg_id
                from session_a2ui_surface
                where session_id = ?
                  and last_updated_msg_id is not null
                  and trim(last_updated_msg_id) <> ''
                order by last_updated_msg_id
                """,
                String.class,
                sessionId));
    }

    @Override
    public void upsert(String sessionId, A2uiSurfaceState surface) {
        jdbcTemplate.update(
                """
                insert into session_a2ui_surface(
                    session_id,
                    surface_id,
                    catalog_id,
                    components_json,
                    data_model_json,
                    last_updated_msg_id,
                    updated_at)
                values (?, ?, ?, ?, ?, ?, ?)
                on duplicate key update
                    catalog_id = values(catalog_id),
                    components_json = values(components_json),
                    data_model_json = values(data_model_json),
                    last_updated_msg_id = values(last_updated_msg_id),
                    updated_at = values(updated_at)
                """,
                sessionId,
                surface.surfaceId(),
                surface.catalogId(),
                writeJson(surface.components()),
                writeJson(surface.dataModel()),
                surface.lastUpdatedMsgId(),
                Timestamp.from(Instant.now()));
    }

    @Override
    public void upsertAll(String sessionId, Collection<A2uiSurfaceState> surfaces) {
        if (surfaces == null || surfaces.isEmpty()) {
            return;
        }
        for (A2uiSurfaceState surface : surfaces) {
            upsert(sessionId, surface);
        }
    }

    @Override
    public void updateLastUpdatedMsgId(String sessionId, Collection<String> surfaceIds, String messageId) {
        List<String> ids = sanitizeIds(surfaceIds);
        if (ids.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = """
                update session_a2ui_surface
                set last_updated_msg_id = ?, updated_at = ?
                where session_id = ?
                  and surface_id in (%s)
                """.formatted(placeholders);
        List<Object> args = new ArrayList<>(ids.size() + 3);
        args.add(messageId);
        args.add(Timestamp.from(Instant.now()));
        args.add(sessionId);
        args.addAll(ids);
        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public void deleteBySessionIdAndSurfaceIds(String sessionId, Collection<String> surfaceIds) {
        List<String> ids = sanitizeIds(surfaceIds);
        if (ids.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = "delete from session_a2ui_surface where session_id = ? and surface_id in (%s)"
                .formatted(placeholders);
        List<Object> args = new ArrayList<>(ids.size() + 1);
        args.add(sessionId);
        args.addAll(ids);
        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        jdbcTemplate.update("delete from session_a2ui_surface where session_id = ?", sessionId);
    }

    private List<Object> readComponents(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("A2UI components 反序列化失败", e);
        }
    }

    private Map<String, Object> readDataModel(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("A2UI dataModel 反序列化失败", e);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("A2UI 状态序列化失败", e);
        }
    }

    private List<String> sanitizeIds(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().filter(v -> v != null && !v.isBlank()).distinct().toList();
    }
}
