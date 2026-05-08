package xiaogj.xzagent.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.UserApiKey;
import xiaogj.xzagent.repository.UserApiKeyRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * 基于 JDBC 的用户 API Key 仓储实现。
 *
 * <p>当前项目仍统一使用 JdbcTemplate，因此这里延续相同风格，避免把单个表
 * 的持久化切成另一套 ORM。
 */
@Repository
public class JdbcUserApiKeyRepository implements UserApiKeyRepository {

    private static final RowMapper<UserApiKey> ROW_MAPPER = new UserApiKeyRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserApiKeyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserApiKey> findByUserId(Long userId) {
        return jdbcTemplate.query(
                        """
                        select id, user_id, key_prefix, key_hash, expires_at, permanent,
                               enabled, created_at, updated_at
                        from user_api_key
                        where user_id = ?
                        limit 1
                        """,
                        ROW_MAPPER,
                        userId)
                .stream()
                .findFirst();
    }

    @Override
    public List<UserApiKey> findByKeyPrefix(String keyPrefix) {
        return jdbcTemplate.query(
                """
                select id, user_id, key_prefix, key_hash, expires_at, permanent,
                       enabled, created_at, updated_at
                from user_api_key
                where key_prefix = ?
                """,
                ROW_MAPPER,
                keyPrefix);
    }

    @Override
    public UserApiKey save(UserApiKey apiKey) {
        if (apiKey.id() == null) {
            Instant now = apiKey.createdAt() != null ? apiKey.createdAt() : Instant.now();
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement(
                        """
                        insert into user_api_key(
                            user_id, key_prefix, key_hash, expires_at, permanent,
                            enabled, created_at, updated_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        new String[] {"id"});
                statement.setLong(1, apiKey.userId());
                statement.setString(2, apiKey.keyPrefix());
                statement.setString(3, apiKey.keyHash());
                statement.setTimestamp(4, toTimestamp(apiKey.expiresAt()));
                statement.setBoolean(5, apiKey.permanent());
                statement.setBoolean(6, apiKey.enabled());
                statement.setTimestamp(7, Timestamp.from(now));
                statement.setTimestamp(
                        8,
                        Timestamp.from(apiKey.updatedAt() != null ? apiKey.updatedAt() : now));
                return statement;
            }, keyHolder);
            Number key = keyHolder.getKey();
            return findById(key != null ? key.longValue() : null).orElseThrow();
        }

        jdbcTemplate.update(
                """
                update user_api_key
                set key_prefix = ?,
                    key_hash = ?,
                    expires_at = ?,
                    permanent = ?,
                    enabled = ?,
                    updated_at = ?
                where id = ?
                """,
                apiKey.keyPrefix(),
                apiKey.keyHash(),
                toTimestamp(apiKey.expiresAt()),
                apiKey.permanent(),
                apiKey.enabled(),
                Timestamp.from(apiKey.updatedAt() != null ? apiKey.updatedAt() : Instant.now()),
                apiKey.id());
        return findById(apiKey.id()).orElseThrow();
    }

    @Override
    public void deleteByUserId(Long userId) {
        jdbcTemplate.update("delete from user_api_key where user_id = ?", userId);
    }

    private Optional<UserApiKey> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jdbcTemplate.query(
                        """
                        select id, user_id, key_prefix, key_hash, expires_at, permanent,
                               enabled, created_at, updated_at
                        from user_api_key
                        where id = ?
                        limit 1
                        """,
                        ROW_MAPPER,
                        id)
                .stream()
                .findFirst();
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    /**
     * 统一映射 API Key 记录。
     */
    private static final class UserApiKeyRowMapper implements RowMapper<UserApiKey> {
        @Override
        public UserApiKey mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp expiresAt = rs.getTimestamp("expires_at");
            return new UserApiKey(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("key_prefix"),
                    rs.getString("key_hash"),
                    expiresAt != null ? expiresAt.toInstant() : null,
                    rs.getBoolean("permanent"),
                    rs.getBoolean("enabled"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant());
        }
    }
}
