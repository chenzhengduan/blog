package xiaogj.xzagent.service;

import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import io.agentscope.core.tool.Toolkit;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import xiaogj.xzagent.repository.SessionActiveSkillRepository;
import org.springframework.stereotype.Service;

/**
 * 管理会话维度的 Skill 激活状态。
 */
@Service
public class SessionActiveSkillService {

    private final SessionActiveSkillRepository repository;
    private final MeterRegistry meterRegistry;

    public SessionActiveSkillService(SessionActiveSkillRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    /**
     * 根据数据库中保存的激活技能列表，重新触发 Skill 装载。
     *
     * <p>AgentScope 当前并不会自动从业务表恢复 Skill 激活状态，所以
     * 这里通过内置的 `load_skill_through_path` 工具补一次装载流程。
     */
    public void restore(String sessionId, Toolkit toolkit) {
        AgentTool loader = toolkit.getTool("load_skill_through_path");
        if (loader == null) {
            return;
        }
        for (String skillId : repository.findActiveSkillIds(sessionId)) {
            loader.callAsync(
                            ToolCallParam.builder()
                                    .toolUseBlock(
                                            ToolUseBlock.builder()
                                                    .id("restore-" + skillId)
                                                    .name("load_skill_through_path")
                                                    .input(java.util.Map.of("skillId", skillId, "path", "SKILL.md"))
                                                    .build())
                                    .input(java.util.Map.of("skillId", skillId, "path", "SKILL.md"))
                                    .build())
                    .block();
        }
    }

    /**
     * 将当前 SkillBox 中处于激活状态的技能集合回写到数据库。
     */
    public void save(String sessionId, SkillBox skillBox) {
        List<String> activeSkillIds = skillBox.getAllSkillIds().stream()
                .filter(skillBox::isSkillActive)
                .sorted()
                .toList();
        List<String> persistedSkillIds = repository.findActiveSkillIds(sessionId);
        if (persistedSkillIds.equals(activeSkillIds)) {
            meterRegistry.counter("xzagent.session.active-skill.save", "result", "skipped").increment();
            return;
        }
        repository.replaceActiveSkills(sessionId, activeSkillIds);
        meterRegistry.counter("xzagent.session.active-skill.save", "result", "executed").increment();
    }

    /**
     * 清理会话相关的技能激活记录。
     */
    public void clear(String sessionId) {
        repository.deleteBySessionId(sessionId);
    }
}
