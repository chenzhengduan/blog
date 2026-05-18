package xiaogj.xzagent.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.UserRemoteSourceCredentialBinding;
import xiaogj.xzagent.repository.UserRemoteSourceCredentialBindingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * 用户级远程来源凭证绑定仓储 JDBC 实现。
 */
@Repository
public class JdbcUserRemoteSourceCredentialBindingRepository
        implements UserRemoteSourceCredentialBindingRepository {

    private static final RowMapper<UserRemoteSourceCredentialBinding> ROW_MAPPER =
            new UserRemoteSourceCredentialBindingRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRemoteSourceCredentialBindingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserRemoteSourceCredentialBinding> findByUserIdAndSourceId(Long userId, String sourceId) {
        return jdbcTemplate.query(
                        """
                        select id, user_id, source_id, credential_id, created_at, updated_at
                        from user_remote_source_credential_binding
                        where user_id = ? and source_id = ?
                        limit 1
                        """,
                        ROW_MAPPER,
                        userId,
                        sourceId)
                .stream()
                .findFirst();
    }

    @Override
    public List<UserRemoteSourceCredentialBinding> findByUserId(Long userId) {
        return jdbcTemplate.query(
                """
                select id, user_id, source_id, credential_id, created_at, updated_at
                from user_remote_source_credential_binding
                where user_id = ?
                order by updated_at desc, source_id asc
                """,
                ROW_MAPPER,
                userId);
    }

    @Override
    public UserRemoteSourceCredentialBinding save(UserRemoteSourceCredentialBinding binding) {
        Optional<UserRemoteSourceCredentialBinding> existing =
                findByUserIdAndSourceId(binding.userId(), binding.sourceId());
        if (existing.isEmpty()) {
            Instant now = binding.createdAt() != null ? binding.createdAt() : Instant.now();
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement(
                        """
                        insert into user_remote_source_credential_binding(
                            user_id, source_id, credential_id, created_at, updated_at
                        ) values (?, ?, ?, ?, ?)
                        """,
                        new String[] {"id"});
                statement.setLong(1, binding.userId());
                statement.setString(2, binding.sourceId());
                statement.setString(3, binding.credentialId());
                statement.setTimestamp(4, Timestamp.from(now));
                statement.setTimestamp(
                        5,
                        Timestamp.from(binding.updatedAt() != null ? binding.updatedAt() : now));
                return statement;
            }, keyHolder);
            Number key = keyHolder.getKey();
            return jdbcTemplate.query(
                            """
                            select id, user_id, source_id, credential_id, created_at, updated_at
                            from user_remote_source_credential_binding
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
                update user_remote_source_credential_binding
                set credential_id = ?,
                    updated_at = ?
                where user_id = ? and source_id = ?
                """,
                binding.credentialId(),
                Timestamp.from(binding.updatedAt() != null ? binding.updatedAt() : Instant.now()),
                binding.userId(),
                binding.sourceId());
        return findByUserIdAndSourceId(binding.userId(), binding.sourceId()).orElseThrow();
    }

    @Override
    public void deleteByUserIdAndSourceId(Long userId, String sourceId) {
        jdbcTemplate.update(
                "delete from user_remote_source_credential_binding where user_id = ? and source_id = ?",
                userId,
                sourceId);
    }

    @Override
    public void deleteByUserIdAndCredentialId(Long userId, String credentialId) {
        jdbcTemplate.update(
                "delete from user_remote_source_credential_binding where user_id = ? and credential_id = ?",
                userId,
                credentialId);
    }

    @Override
    public void deleteBySourceId(String sourceId) {
        jdbcTemplate.update(
                "delete from user_remote_source_credential_binding where source_id = ?",
                sourceId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jdbcTemplate.update(
                "delete from user_remote_source_credential_binding where user_id = ?",
                userId);
    }

    @Override
    public boolean existsByUserIdAndCredentialId(Long userId, String credentialId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(1)
                from user_remote_source_credential_binding
                where user_id = ? and credential_id = ?
                """,
                Integer.class,
                userId,
                credentialId);
        return count != null && count > 0;
    }

    /**
     * 统一将数据库记录映射为领域对象。
     */
    private static final class UserRemoteSourceCredentialBindingRowMapper
            implements RowMapper<UserRemoteSourceCredentialBinding> {
        @Override
        public UserRemoteSourceCredentialBinding mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            return new UserRemoteSourceCredentialBinding(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("source_id"),
                    rs.getString("credential_id"),
                    createdAt != null ? createdAt.toInstant() : null,
                    updatedAt != null ? updatedAt.toInstant() : null);
        }
    }
}
