package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户级远程来源凭证绑定更新请求。
 */
public record UserRemoteSourceCredentialBindingUpdateRequest(
        @NotBlank String credentialId) {
}
