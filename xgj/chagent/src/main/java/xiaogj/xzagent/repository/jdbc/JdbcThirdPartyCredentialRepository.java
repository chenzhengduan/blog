package xiaogj.xzagent.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.ThirdPartyCredential;
import xiaogj.xzagent.repository.ThirdPartyCredentialRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * 用户级第三方凭证仓储 JDBC 实现。
 *
 * <p>这里继续沿用按 `(user_id, credential_id)` 做 upsert 的方式，保证第一版
 * 在语义清晰的同时，仍保持较低的仓储复杂度。
 */
@Repository
public class JdbcThirdPartyCredentialRepository implements ThirdPartyCredentialRepository {

    private static final RowMapper<ThirdPartyCredential> ROW_MAPPER =
            new ThirdPartyCredentialRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public JdbcThirdPartyCredentialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ThirdPartyCredential> findByUserIdAndCredentialId(Long userId, String credentialId) {
        return jdbcTemplate.query(
                        """
                        select id, user_id, credential_id, name, description, headers_json_encrypted,
                               enabled, created_at, updated_at
                        from user_third_party_credential
                        where user_id = ? and credential_id = ?
                        limit 1
                        """,
                        ROW_MAPPER,
                        userId,
                        credentialId)
                .stream()
                .findFirst();
    }

    @Override
    public List<ThirdPartyCredential> findByUserId(Long userId) {
        return jdbcTemplate.query(
                """
                select id, user_id, credential_id, name, description, headers_json_encrypted,
                       enabled, created_at, updated_at
                from user_third_party_credential
                where user_id = ?
                order by updated_at desc, credential_id asc
                """,
                ROW_MAPPER,
                userId);
    }

    @Override
    public ThirdPartyCredential save(ThirdPartyCredential credential) {
        Optional<ThirdPartyCredential> existing =
                findByUserIdAndCredentialId(credential.userId(), credential.credentialId());
        if (existing.isEmpty()) {
            Instant now = credential.createdAt() != null ? credential.createdAt() : Instant.now();
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement(
                        """
                        insert into user_third_party_credential(
                            user_id, credential_id, name, description, headers_json_encrypted,
                            enabled, created_at, updated_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        new String[] {"id"});
                statement.setLong(1, credential.userId());
                statement.setString(2, credential.credentialId());
                statement.setString(3, credential.name());
                statement.setString(4, credential.description());
                statement.setString(5, credential.headersJsonEncrypted());
                statement.setBoolean(6, credential.enabled());
                statement.setTimestamp(7, Timestamp.from(now));
                statement.setTimestamp(
                        8,
                        Timestamp.from(credential.updatedAt() != null ? credential.updatedAt() : now));
                return statement;
            }, keyHolder);
            Number key = keyHolder.getKey();
            return jdbcTemplate.query(
                            """
                            select id, user_id, credential_id, name, description, headers_json_encrypted,
                                   enabled, created_at, updated_at
                            from user_third_party_credential
                            where id = ?
                            """,
                            ROW_MAPPER,
                            key != null ? key.longValue() : -1L)
                    .stream()
                    .findFirst()
                    .orElseThrow();
        }

        jdbcTemplate.update(
                """
                update user_third_party_credential
                set name = ?,
                    description = ?,
                    headers_json_encrypted = ?,
                    enabled = ?,
                    updated_at = ?
                where user_id = ? and credential_id = ?
                """,
                credential.name(),
                credential.description(),
                credential.headersJsonEncrypted(),
                credential.enabled(),
                Timestamp.from(credential.updatedAt() != null ? credential.updatedAt() : Instant.now()),
                credential.userId(),
                credential.credentialId());
        return findByUserIdAndCredentialId(credential.userId(), credential.credentialId()).orElseThrow();
    }

    @Override
    public void deleteByUserIdAndCredentialId(Long userId, String credentialId) {
        jdbcTemplate.update(
                "delete from user_third_party_credential where user_id = ? and credential_id = ?",
                userId,
                credentialId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jdbcTemplate.update("delete from user_third_party_credential where user_id = ?", userId);
    }

    /**
     * 统一把数据库记录映射为领域对象。
     */
    private static final class ThirdPartyCredentialRowMapper implements RowMapper<ThirdPartyCredential> {
        @Override
        public ThirdPartyCredential mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new ThirdPartyCredential(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("credential_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("headers_json_encrypted"),
                    rs.getBoolean("enabled"),
                    createdAt != null ? createdAt.toInstant() : null,
                    updatedAt != null ? updatedAt.toInstant() : null);
        }
    }
}
