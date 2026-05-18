package xiaogj.xzagent.service;

import xiaogj.xzagent.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 安全层用户查询服务。
 */
@Service
public class UserSecurityService {

    private final SysUserRepository sysUserRepository;

    public UserSecurityService(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
    }

    /**
     * 按用户名加载认证所需的最小用户信息。
     */
    public Mono<UserPrincipal> loadUserByUsername(String username) {
        return Mono.justOrEmpty(sysUserRepository.findByUsername(username))
                .map(user -> new UserPrincipal(
                        user.id(),
                        user.username(),
                        user.role(),
                        user.enabled()))
                .filter(UserPrincipal::enabled);
    }
}
