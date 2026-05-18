package xiaogj.xzagent.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/**
 * 用户 API Key 状态响应。
 *
 * <p>默认不返回完整明文 Key，只有创建或重置时才会在 `plainTextKey` 中返回一次。
 */
public record UserApiKeyResponse(
        boolean exists,
        String maskedKey,
        String plainTextKey,
        boolean permanent,
        boolean enabled,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant expiresAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant updatedAt) {
}
