package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import java.util.List;
import xiaogj.xzagent.service.SkillAdminService;
import xiaogj.xzagent.web.dto.SkillCatalogResponse;
import xiaogj.xzagent.web.dto.SkillDetailResponse;
import xiaogj.xzagent.web.dto.SkillInlineUpsertRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Skill 目录管理接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/skills")
public class SkillCatalogController {

    private final SkillAdminService skillAdminService;

    public SkillCatalogController(SkillAdminService skillAdminService) {
        this.skillAdminService = skillAdminService;
    }

    /**
     * 列出当前可供 Agent 选择的全部 Skill。
     */
    @GetMapping
    public List<SkillCatalogResponse> listSkills() {
        return skillAdminService.listSkills();
    }

    /**
     * 查询单个 Skill 详情。
     */
    @GetMapping("/{skillId}")
    public SkillDetailResponse getSkill(@PathVariable String skillId) {
        return skillAdminService.getSkill(skillId);
    }

    /**
     * 通过纯文本方式新建 Skill。
     */
    @PostMapping("/inline")
    public SkillDetailResponse createSkill(@Valid @RequestBody SkillInlineUpsertRequest request) {
        return skillAdminService.saveInline(null, request);
    }

    /**
     * 覆盖更新指定 Skill。
     */
    @PutMapping("/{skillId}/inline")
    public SkillDetailResponse updateSkill(
            @PathVariable String skillId,
            @Valid @RequestBody SkillInlineUpsertRequest request) {
        return skillAdminService.saveInline(skillId, request);
    }

    /**
     * 通过 zip/skill 包上传 Skill。
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<SkillDetailResponse> uploadSkill(
            @RequestParam(value = "skillId", required = false) String skillId,
            @RequestParam(value = "force", defaultValue = "false") boolean force,
            @RequestPart("file") FilePart file) {
        return skillAdminService.savePackage(skillId, file, force);
    }

    /**
     * 删除指定 Skill。
     */
    @DeleteMapping("/{skillId}")
    public void deleteSkill(@PathVariable String skillId) {
        skillAdminService.deleteSkill(skillId);
    }
}
