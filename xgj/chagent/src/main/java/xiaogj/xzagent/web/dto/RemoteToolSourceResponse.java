package xiaogj.xzagent.web.dto;

import java.time.Instant;

/**
 * 远程工具源响应。
 */
public record RemoteToolSourceResponse(
        String sourceId,
        String name,
        String metaUrl,
        boolean uploaded,
        boolean enabled,
        long timeoutSeconds,
        long refreshIntervalSeconds,
        Instant updatedAt) {
}

