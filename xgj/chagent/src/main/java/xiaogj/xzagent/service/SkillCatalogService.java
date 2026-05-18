package xiaogj.xzagent.service;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import java.util.List;
import xiaogj.xzagent.web.dto.SkillCatalogResponse;
import org.springframework.stereotype.Service;

/**
 * Skill 目录服务。
 *
 * <p>该服务只负责暴露当前运行时可识别的 Skill 清单，不掺杂绑定关系和会话状态。
 * 这样设置页可以先完成“给 Agent 选择哪些 Skill”，再继续看每个 Skill 能接入
 * 哪些全局能力来源。
 */
@Service
public class SkillCatalogService {

    private final AgentSkillRepository skillRepository;

    public SkillCatalogService(AgentSkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    /**
     * 返回当前 Skill 仓库中的全部 Skill。
     *
     * <p>对管理界面来说，平台内的 Skill 逻辑标识统一使用 Skill 名称，不直接
     * 暴露 AgentScope 运行时的 `name_source` 组合 ID。这样数据库绑定表、
     * 前端配置页与后续迁移脚本都只需要围绕稳定的 `name` 工作。
     */
    public List<SkillCatalogResponse> listSkills() {
        return skillRepository.getAllSkills().stream()
                .map(this::toResponse)
                .toList();
    }

    private SkillCatalogResponse toResponse(AgentSkill skill) {
        return new SkillCatalogResponse(
                skill.getName(),
                skill.getName(),
                skill.getDescription(),
                skill.getSource());
    }
}
