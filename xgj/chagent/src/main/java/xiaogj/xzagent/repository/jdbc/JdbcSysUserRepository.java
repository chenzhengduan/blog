package xiaogj.xzagent.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.SysUser;
import xiaogj.xzagent.model.UserRole;
import xiaogj.xzagent.repository.SysUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * 基于 JDBC 的系统用户仓储实现。
 *
 * <p>当前项目整体采用 JDBCTemplate 风格，这里延续同一实现方式，避免在
 * 账号体系中混入另一套 ORM 使用习惯。
 */
@Repository
public class JdbcSysUserRepository implements SysUserRepository {

    private static final RowMapper<SysUser> ROW_MAPPER = new SysUserRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public JdbcSysUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<SysUser> findByUsername(String username) {
        return jdbcTemplate.query(
                        """
                        select id, username, password_hash, role, enabled, created_at, updated_at
                        from sys_user
                        where username = ?
                        limit 1
                        """,
                        ROW_MAPPER,
                        username)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<SysUser> findById(Long id) {
        return jdbcTemplate.query(
                        """
                        select id, username, password_hash, role, enabled, created_at, updated_at
                        from sys_user
                        where id = ?
                        limit 1
                        """,
                        ROW_MAPPER,
                        id)
                .stream()
                .findFirst();
    }

    @Override
    public List<SysUser> findAll() {
        return jdbcTemplate.query(
                """
                select id, username, password_hash, role, enabled, created_at, updated_at
                from sys_user
                order by created_at asc
                """,
                ROW_MAPPER);
    }

    @Override
    public long countByRole(UserRole role) {
        Long count = jdbcTemplate.queryForObject(
                "select count(*) from sys_user where role = ? and enabled = true",
                Long.class,
                role.name());
        return count != null ? count : 0L;
    }

    @Override
    public SysUser save(SysUser user) {
        if (user.id() == null) {
            Instant now = user.createdAt() != null ? user.createdAt() : Instant.now();
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement(
                        """
                        insert into sys_user(
                            username, password_hash, role, enabled, created_at, updated_at
                        ) values (?, ?, ?, ?, ?, ?)
                        """,
                        new String[] {"id"});
                statement.setString(1, user.username());
                statement.setString(2, user.passwordHash());
                statement.setString(3, user.role().name());
                statement.setBoolean(4, user.enabled());
                statement.setTimestamp(5, Timestamp.from(now));
                statement.setTimestamp(6, Timestamp.from(user.updatedAt() != null ? user.updatedAt() : now));
                return statement;
            }, keyHolder);
            Number key = keyHolder.getKey();
            return findById(key != null ? key.longValue() : null).orElseThrow();
        }

        jdbcTemplate.update(
                """
                update sys_user
                set username = ?,
                    password_hash = ?,
                    role = ?,
                    enabled = ?,
                    updated_at = ?
                where id = ?
                """,
                user.username(),
                user.passwordHash(),
                user.role().name(),
                user.enabled(),
                Timestamp.from(user.updatedAt() != null ? user.updatedAt() : Instant.now()),
                user.id());
        return findById(user.id()).orElseThrow();
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from sys_user where id = ?", id);
    }

    /**
     * 统一映射数据库用户记录，避免多处重复拼装角色和时间字段。
     */
    private static final class SysUserRowMapper implements RowMapper<SysUser> {
        @Override
        public SysUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SysUser(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    UserRole.valueOf(rs.getString("role")),
                    rs.getBoolean("enabled"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant());
        }
    }
}
