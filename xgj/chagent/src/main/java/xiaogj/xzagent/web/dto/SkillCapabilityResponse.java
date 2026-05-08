package xiaogj.xzagent.web.dto;

import java.util.List;

/**
 * Skill 能力总览响应。
 *
 * <p>用于设置页展示“某个 Skill 当前挂了哪些本地工具、MCP 服务与远程来源”，
 * 帮助用户理解能力是如何分层接入默认助手的。
 */
public record SkillCapabilityResponse(
        String skillId,
        List<String> localTools,
        List<String> mcpServers,
        List<String> remoteSources) {
}
