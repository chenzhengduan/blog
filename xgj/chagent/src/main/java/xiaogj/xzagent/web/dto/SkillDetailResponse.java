package xiaogj.xzagent.web.dto;

import java.util.Map;

/**
 * Skill 详情响应。
 *
 * <p>管理端编辑 Skill 时，需要看到完整的 `SKILL.md` 文本以及附属资源内容，
 * 因此这里提供比目录列表更完整的响应结构。
 */
public record SkillDetailResponse(
        String skillId,
        String name,
        String description,
        String skillContent,
        Map<String, String> resources,
        String source) {
}
