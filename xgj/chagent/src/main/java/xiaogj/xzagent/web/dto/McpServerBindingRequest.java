package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * MCP 与 Skill 的绑定请求。
 */
public record McpServerBindingRequest(
        @NotBlank String skillId,
        @NotBlank String serverId,
        List<String> enableTools,
        List<String> disableTools,
        @NotNull Boolean enabled) {
}

