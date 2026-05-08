package xiaogj.xzagent.web.dto;

import java.util.List;

/**
 * Skill 能力来源更新请求。
 *
 * <p>这里约束的是“某个 Skill 从全局能力池中选中了哪些来源”，而不是直接让
 * 前端操作底层绑定表。这样交互层仍然保持 Skill 视角，配置方向更符合用户心智。
 */
public record SkillBindingUpdateRequest(
        List<String> localToolNames,
        List<String> mcpServerIds,
        List<String> remoteSourceIds) {
}
