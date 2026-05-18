package xiaogj.xzagent.infrastructure.session;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.state.State;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MysqlAgentSession implements Session {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MysqlAgentSession(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void save(SessionKey sessionKey, String key, State value) {
        touchSession(sessionKey);
        // SINGLE 和 LIST 分开存储，避免同一个 state_key 同时出现
        // 单值与列表时产生读取歧义。
        delete(sessionKey, key, "SINGLE");
        jdbcTemplate.update(
                """
                insert into agent_session_state(session_id, state_key, state_scope, list_index, class_name, content_json, updated_at)
                values (?, ?, 'SINGLE', 0, ?, ?, current_timestamp)
                """,
                sessionKey.toIdentifier(),
                key,
                value.getClass().getName(),
                writeJson(value));
    }

    @Override
    public void save(SessionKey sessionKey, String key, List<? extends State> values) {
        touchSession(sessionKey);
        delete(sessionKey, key, "LIST");
        for (int i = 0; i < values.size(); i++) {
            State value = values.get(i);
            jdbcTemplate.update(
                    """
                    insert into agent_session_state(session_id, state_key, state_scope, list_index, class_name, content_json, updated_at)
                    values (?, ?, 'LIST', ?, ?, ?, current_timestamp)
                    """,
                    sessionKey.toIdentifier(),
                    key,
                    i,
                    value.getClass().getName(),
                    writeJson(value));
        }
    }

    @Override
    public <T extends State> Optional<T> get(SessionKey sessionKey, String key, Class<T> type) {
        List<T> rows = jdbcTemplate.query(
                """
                select content_json
                from agent_session_state
                where session_id = ? and state_key = ? and state_scope = 'SINGLE'
                order by list_index
                """,
                (rs, rowNum) -> readJson(rs.getString("content_json"), type),
                sessionKey.toIdentifier(),
                key);
        return rows.stream().findFirst();
    }

    @Override
    public <T extends State> List<T> getList(SessionKey sessionKey, String key, Class<T> itemType) {
        return jdbcTemplate.query(
                """
                select content_json
                from agent_session_state
                where session_id = ? and state_key = ? and state_scope = 'LIST'
                order by list_index
                """,
                (rs, rowNum) -> readJson(rs.getString("content_json"), itemType),
                sessionKey.toIdentifier(),
                key);
    }

    @Override
    public boolean exists(SessionKey sessionKey) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from agent_session where session_id = ?",
                Integer.class,
                sessionKey.toIdentifier());
        return count != null && count > 0;
    }

    @Override
    public void delete(SessionKey sessionKey) {
        jdbcTemplate.update("delete from agent_session_state where session_id = ?", sessionKey.toIdentifier());
        jdbcTemplate.update("delete from agent_session where session_id = ?", sessionKey.toIdentifier());
    }

    @Override
    public void delete(SessionKey sessionKey, String key) {
        jdbcTemplate.update(
                "delete from agent_session_state where session_id = ? and state_key = ?",
                sessionKey.toIdentifier(),
                key);
    }

    @Override
    public Set<SessionKey> listSessionKeys() {
        return Set.copyOf(
                jdbcTemplate.query(
                        "select session_id from agent_session",
                        (rs, rowNum) -> SimpleSessionKey.of(rs.getString("session_id"))));
    }

    private void delete(SessionKey sessionKey, String key, String stateScope) {
        jdbcTemplate.update(
                """
                delete from agent_session_state
                where session_id = ? and state_key = ? and state_scope = ?
                """,
                sessionKey.toIdentifier(),
                key,
                stateScope);
    }

    public void recordUser(String sessionId, Long userId) {
        jdbcTemplate.update(
                """
                insert into agent_session(session_id, user_id, created_at, updated_at)
                values (?, ?, current_timestamp, current_timestamp)
                on duplicate key update user_id = ?, updated_at = current_timestamp
                """,
                sessionId, userId, userId);
    }

    /**
     * 查询指定会话的最后更新时间。
     *
     * @param sessionId 会话标识
     * @return 若会话存在则返回 updated_at，否则返回 empty
     */
    public Optional<java.time.Instant> findUpdatedAt(String sessionId) {
        List<java.time.Instant> results = jdbcTemplate.query(
                "select updated_at from agent_session where session_id = ?",
                (rs, rowNum) -> rs.getTimestamp("updated_at").toInstant(),
                sessionId);
        return results.stream().findFirst();
    }

    public Optional<String> findLatestSessionIdByUserId(Long userId) {
        List<String> results = jdbcTemplate.query(
                """
                select session_id from agent_session
                where user_id = ?
                order by updated_at desc
                limit 1
                """,
                (rs, rowNum) -> rs.getString("session_id"),
                userId);
        return results.stream().findFirst();
    }

    private void touchSession(SessionKey sessionKey) {
        jdbcTemplate.update(
                """
                insert into agent_session(session_id, created_at, updated_at)
                values (?, current_timestamp, current_timestamp)
                on duplicate key update updated_at = current_timestamp
                """,
                sessionKey.toIdentifier());
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("状态序列化失败", e);
        }
    }

    private <T> T readJson(String value, Class<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (Exception e) {
            throw new IllegalStateException("状态反序列化失败", e);
        }
    }
}
