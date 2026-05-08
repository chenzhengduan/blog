package xiaogj.xzagent.service;

import java.time.Instant;
import xiaogj.xzagent.model.SysUser;
import xiaogj.xzagent.model.UserRole;
import xiaogj.xzagent.repository.SysUserRepository;
import xiaogj.xzagent.web.dto.AuthBootstrapRequest;
import xiaogj.xzagent.web.dto.AuthBootstrapStatusResponse;
import xiaogj.xzagent.web.dto.AuthLoginRequest;
import xiaogj.xzagent.web.dto.AuthLoginResponse;
import xiaogj.xzagent.web.dto.AuthUserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 登录与初始化服务。
 *
 * <p>该服务负责首个超管初始化、登录校验和当前用户信息输出，不承担用户管理
 * 列表等后台职责。
 */
@Service
public class AuthService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            SysUserRepository sysUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * 返回当前系统是否仍需要初始化首个超管。
     */
    public AuthBootstrapStatusResponse getBootstrapStatus() {
        boolean bootstrapRequired = sysUserRepository.countByRole(UserRole.SUPER_ADMIN) == 0;
        return new AuthBootstrapStatusResponse(bootstrapRequired);
    }

    /**
     * 初始化首个超管账号。
     */
    public AuthUserResponse bootstrap(AuthBootstrapRequest request) {
        if (sysUserRepository.countByRole(UserRole.SUPER_ADMIN) > 0) {
            throw new IllegalStateException("系统已初始化超管账号，不能重复执行 bootstrap");
        }
        SysUser user = new SysUser(
                null,
                request.username().trim(),
                passwordEncoder.encode(request.password()),
                UserRole.SUPER_ADMIN,
                true,
                Instant.now(),
                Instant.now());
        SysUser saved = sysUserRepository.save(user);
        return toUserResponse(saved);
    }

    /**
     * 用户登录。
     */
    public AuthLoginResponse login(AuthLoginRequest request) {
        SysUser user = sysUserRepository.findByUsername(request.username().trim())
                .filter(SysUser::enabled)
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return issueLoginResponse(user);
    }

    public AuthLoginResponse issueLoginResponse(SysUser user) {
        UserPrincipal principal = new UserPrincipal(
                user.id(),
                user.username(),
                user.role(),
                user.enabled());
        return new AuthLoginResponse(
                jwtService.generateToken(principal),
                toUserResponse(user),
                null);
    }

    /**
     * 将安全上下文中的用户转换成前端所需的轻量信息。
     */
    public AuthUserResponse getCurrentUser(UserPrincipal principal) {
        return new AuthUserResponse(
                principal.userId(),
                principal.username(),
                principal.role().name(),
                principal.enabled());
    }

    private AuthUserResponse toUserResponse(SysUser user) {
        return new AuthUserResponse(
                user.id(),
                user.username(),
                user.role().name(),
                user.enabled());
    }
}
