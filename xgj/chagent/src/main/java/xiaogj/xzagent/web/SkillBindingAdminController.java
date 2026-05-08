package xiaogj.xzagent.web;

import xiaogj.xzagent.service.SkillBindingAdminService;
import xiaogj.xzagent.web.dto.SkillBindingUpdateRequest;
import xiaogj.xzagent.web.dto.SkillCapabilityResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Skill 绑定管理接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/skills")
public class SkillBindingAdminController {

    private final SkillBindingAdminService skillBindingAdminService;

    public SkillBindingAdminController(SkillBindingAdminService skillBindingAdminService) {
        this.skillBindingAdminService = skillBindingAdminService;
    }

    /**
     * 从 Skill 视角更新它可见的全局能力来源。
     */
    @PutMapping("/{skillId}/bindings")
    public SkillCapabilityResponse updateBindings(
            @PathVariable String skillId,
            @RequestBody SkillBindingUpdateRequest request) {
        return skillBindingAdminService.updateBindings(skillId, request);
    }
}
