package xiaogj.xzagent.model;

import java.time.Instant;

/**
 * 系统用户定义。
 *
 * <p>该对象对应 `sys_user` 表，承担登录、角色控制和用户级配置归属三项
 * 核心职责。密码只保存哈希值，不保存明文。
 *
 * @param id 用户主键
 * @param username 登录名
 * @param passwordHash 密码哈希
 * @param role 角色
 * @param enabled 是否启用
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record SysUser(
        Long id,
        String username,
        String passwordHash,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt) {
}
