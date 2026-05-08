package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Skill 文本上传请求。
 *
 * <p>前端可直接提交 `SKILL.md` 内容与附属资源映射，后端再统一解析并落库。
 */
public record SkillInlineUpsertRequest(
        @NotBlank(message = "skillMd 不能为空")
        String skillMd,
        Map<String, String> resources,
        Boolean force) {
}
