package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * MCP Server 保存请求。
 */
public record McpServerRequest(
        @NotBlank String serverId,
        @NotBlank String name,
        @NotBlank String transportType,
        @NotNull Boolean enabled,
        Map<String, Object> config,
        Map<String, String> headers,
        Map<String, String> queryParams,
        @Min(1) long timeoutSeconds,
        @Min(1) long initializationTimeoutSeconds,
        List<McpServerBindingRequest> skillBindings) {
}

