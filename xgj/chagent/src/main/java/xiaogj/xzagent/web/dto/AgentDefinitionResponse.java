package xiaogj.xzagent.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * Agent 配置响应。
 */
public record AgentDefinitionResponse(
        String agentId,
        String name,
        String systemPrompt,
        String agentsMd,
        int maxIterations,
        List<String> skillIds,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt) {
}
