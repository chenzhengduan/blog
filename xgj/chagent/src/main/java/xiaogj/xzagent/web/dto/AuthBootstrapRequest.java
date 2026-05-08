package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 首次初始化超管请求。
 */
public record AuthBootstrapRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码至少需要 6 位")
        String password) {
}
