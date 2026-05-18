package xiaogj.xzagent.model;

import java.util.List;

/**
 * Skill 与 MCP Server 工具白名单的绑定关系。
 */
public record SkillMcpBinding(
        String skillId,
        String serverId,
        List<String> enableTools,
        List<String> disableTools,
        boolean enabled) {
}
