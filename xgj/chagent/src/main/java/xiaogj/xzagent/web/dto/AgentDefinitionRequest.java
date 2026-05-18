package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Agent 配置更新请求。
 */
public record AgentDefinitionRequest(
        String agentId,
        @NotBlank String name,
        @NotBlank String systemPrompt,
        String agentsMd,
        @Min(1) int maxIterations,
        List<String> skillIds,
        @NotNull Boolean enabled) {
}
