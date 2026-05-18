package xiaogj.xzagent.web.dto;

/**
 * Skill 目录响应。
 *
 * <p>设置页需要知道“当前系统有哪些 Skill 可供 Agent 选择”，因此单独暴露
 * 这个只读目录接口，避免前端把 Skill 能力地图误当成 Skill 列表来使用。
 */
public record SkillCatalogResponse(
        String skillId,
        String name,
        String description,
        String source) {
}
