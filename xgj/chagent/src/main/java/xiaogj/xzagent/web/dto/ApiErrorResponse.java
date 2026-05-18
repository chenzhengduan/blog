package xiaogj.xzagent.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * 统一接口错误响应。
 *
 * <p>默认 Spring WebFlux 错误响应对调试不够友好，尤其是在表单校验和参数绑定失败时，
 * 前端只能拿到一个笼统的 `400 Bad Request`。该结构显式返回 `message` 与
 * `details`，便于前后端共同定位问题。
 */
public record ApiErrorResponse(
        Instant timestamp,
        String path,
        int status,
        String error,
        String message,
        String requestId,
        List<String> details) {
}
