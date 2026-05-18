package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public record PluginLoginRequest(
        @NotBlank(message = "uid 不能为空")
        String uid,
        @NotBlank(message = "thirdPartyCredentialId 不能为空")
        String thirdPartyCredentialId,
        @NotEmpty(message = "thirdPartyCredentialHeaders 不能为空")
        Map<String, String> thirdPartyCredentialHeaders) {
}
