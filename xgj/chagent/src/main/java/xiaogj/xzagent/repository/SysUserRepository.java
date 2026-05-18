package xiaogj.xzagent.repository;

import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.SysUser;
import xiaogj.xzagent.model.UserRole;

/**
 * 系统用户仓储。
 */
public interface SysUserRepository {

    /**
     * 按用户名查询用户。
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 按主键查询用户。
     */
    Optional<SysUser> findById(Long id);

    /**
     * 列出全部用户。
     */
    List<SysUser> findAll();

    /**
     * 统计指定角色的用户数量。
     */
    long countByRole(UserRole role);

    /**
     * 新增或更新用户。
     */
    SysUser save(SysUser user);

    /**
     * 删除指定用户。
     */
    void deleteById(Long id);
}
