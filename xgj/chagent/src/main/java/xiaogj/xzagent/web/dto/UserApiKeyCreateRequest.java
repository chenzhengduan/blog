package xiaogj.xzagent.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import java.time.Instant;

/**
 * 用户 API Key 创建或重置请求。
 */
public record UserApiKeyCreateRequest(
        boolean permanent,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        @Future(message = "过期时间必须晚于当前时间")
        Instant expiresAt) {
}
