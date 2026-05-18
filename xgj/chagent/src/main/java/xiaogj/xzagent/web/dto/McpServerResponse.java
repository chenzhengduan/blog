package xiaogj.xzagent.web.dto;

import java.util.Map;
import java.util.List;

/**
 * MCP Server 响应。
 */
public record McpServerResponse(
        String serverId,
        String name,
        String transportType,
        boolean enabled,
        Map<String, Object> config,
        Map<String, String> headers,
        Map<String, String> queryParams,
        long timeoutSeconds,
        long initializationTimeoutSeconds,
        List<McpServerBindingRequest> skillBindings) {
}
