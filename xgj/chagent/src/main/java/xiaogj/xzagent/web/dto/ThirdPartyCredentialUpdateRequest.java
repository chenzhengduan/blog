package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 更新第三方凭证请求。
 *
 * @param name 展示名称
 * @param description 描述
 * @param headers 请求头键值对
 * @param enabled 是否启用
 */
public record ThirdPartyCredentialUpdateRequest(
        @NotBlank String name,
        String description,
        Map<String, String> headers,
        Boolean enabled) {
}
