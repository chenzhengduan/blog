package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import xiaogj.xzagent.service.PluginLoginService;
import xiaogj.xzagent.web.dto.AuthLoginResponse;
import xiaogj.xzagent.web.dto.PluginLoginRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 私有认证控制器。
 *
 * <p>该控制器仅承载需要走私有路径暴露的认证接口，
 * 以便配合现有网关规则将对应接口限制为内网/直连调用。
 */
@RestController
@RequestMapping("/xzagent/private/api/auth")
public class AuthPrivateController {

    private final PluginLoginService pluginLoginService;

    public AuthPrivateController(PluginLoginService pluginLoginService) {
        this.pluginLoginService = pluginLoginService;
    }

    /**
     * 插件临时登录。
     *
     * <p>该接口复用现有插件登录/自动注册逻辑，
     * 但通过私有路径暴露，避免继续走公开认证接口前缀。
     */
    @PostMapping("/plugin-login")
    public AuthLoginResponse pluginLogin(@Valid @RequestBody PluginLoginRequest request) {
        return pluginLoginService.loginOrRegister(request);
    }
}
