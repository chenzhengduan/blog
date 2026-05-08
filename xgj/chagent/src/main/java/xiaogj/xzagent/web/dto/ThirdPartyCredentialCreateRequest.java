package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 新建第三方凭证请求。
 *
 * @param credentialId 用户级稳定标识
 * @param name 展示名称
 * @param description 描述
 * @param headers 请求头键值对
 * @param enabled 是否启用
 */
public record ThirdPartyCredentialCreateRequest(
        @NotBlank String credentialId,
        @NotBlank String name,
        String description,
        Map<String, String> headers,
        Boolean enabled) {
}
