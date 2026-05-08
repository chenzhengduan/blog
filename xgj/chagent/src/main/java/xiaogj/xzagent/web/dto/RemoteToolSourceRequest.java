package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 基于 URL 的远程工具源保存请求。
 */
public record RemoteToolSourceRequest(
        @NotBlank String sourceId,
        @NotBlank String name,
        @NotBlank String metaUrl,
        @NotNull Boolean enabled,
        Map<String, String> headers,
        @Min(1) long timeoutSeconds,
        @Min(1) long refreshIntervalSeconds) {
}

