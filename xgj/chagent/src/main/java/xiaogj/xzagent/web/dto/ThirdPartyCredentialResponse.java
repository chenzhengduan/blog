package xiaogj.xzagent.web.dto;

import java.time.Instant;
import java.util.Map;

/**
 * 第三方凭证响应。
 */
public record ThirdPartyCredentialResponse(
        String credentialId,
        String name,
        String description,
        Map<String, String> headers,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt) {
}
