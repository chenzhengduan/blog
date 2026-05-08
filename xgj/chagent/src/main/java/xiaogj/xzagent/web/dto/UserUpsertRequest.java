package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户新增请求。
 */
public record UserUpsertRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码至少需要 6 位")
        String password,
        @Pattern(
                regexp = "SUPER_ADMIN|USER",
                message = "角色只能是 SUPER_ADMIN 或 USER")
        String role,
        boolean enabled) {
}
