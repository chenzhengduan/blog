package xiaogj.xzagent.web;

import java.util.List;
import xiaogj.xzagent.service.SkillCapabilityService;
import xiaogj.xzagent.web.dto.SkillCapabilityResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Skill 能力总览接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/skill-capabilities")
public class SkillCapabilityController {

    private final SkillCapabilityService skillCapabilityService;

    public SkillCapabilityController(SkillCapabilityService skillCapabilityService) {
        this.skillCapabilityService = skillCapabilityService;
    }

    /**
     * 列出全部 Skill 的能力映射关系。
     */
    @GetMapping
    public List<SkillCapabilityResponse> listCapabilities() {
        return skillCapabilityService.listCapabilities();
    }
}
