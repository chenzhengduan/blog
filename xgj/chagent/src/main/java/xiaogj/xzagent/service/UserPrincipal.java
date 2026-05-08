package xiaogj.xzagent.service;

import java.util.Collection;
import java.util.List;
import xiaogj.xzagent.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * 当前登录用户的安全上下文快照。
 *
 * <p>这里不直接暴露数据库实体，是为了避免认证层误依赖密码哈希等敏感字段。
 */
public record UserPrincipal(
        Long userId,
        String username,
        UserRole role,
        boolean enabled) {

    /**
     * 转换为 Spring Security 授权信息。
     */
    public Collection<? extends GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
