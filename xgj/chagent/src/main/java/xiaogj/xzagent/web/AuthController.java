package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import xiaogj.xzagent.service.AuthService;
import xiaogj.xzagent.service.UserPrincipal;
import xiaogj.xzagent.web.dto.AuthBootstrapRequest;
import xiaogj.xzagent.web.dto.AuthBootstrapStatusResponse;
import xiaogj.xzagent.web.dto.AuthLoginRequest;
import xiaogj.xzagent.web.dto.AuthLoginResponse;
import xiaogj.xzagent.web.dto.AuthUserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器。
 */
@RestController
@RequestMapping("/xzagent/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 查询是否需要初始化首个超管。
     */
    @GetMapping("/bootstrap/status")
    public AuthBootstrapStatusResponse getBootstrapStatus() {
        return authService.getBootstrapStatus();
    }

    /**
     * 初始化首个超管账号。
     */
    @PostMapping("/bootstrap")
    public AuthUserResponse bootstrap(@Valid @RequestBody AuthBootstrapRequest request) {
        return authService.bootstrap(request);
    }

    /**
     * 用户登录。
     */
    @PostMapping("/login")
    public AuthLoginResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    /**
     * 当前版本采用无状态 JWT，登出由前端清 token 即可。
     */
    @PostMapping("/logout")
    public void logout() {
        // JWT 无状态登出无需服务端保存额外状态，保留该接口是为了让前端
        // 有稳定的退出动作入口。
    }

    /**
     * 查询当前登录用户信息。
     */
    @GetMapping("/me")
    public AuthUserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.getCurrentUser(principal);
    }
}
