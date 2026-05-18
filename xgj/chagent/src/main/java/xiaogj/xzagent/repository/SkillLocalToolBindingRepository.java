package xiaogj.xzagent.repository;

import xiaogj.xzagent.model.SkillLocalToolBinding;
import java.util.List;

public interface SkillLocalToolBindingRepository {
    /**
     * 查询所有启用的本地 Tool 绑定配置。
     */
    List<SkillLocalToolBinding> findEnabledBindings();

    /**
     * 使用新的绑定列表覆盖指定 Skill 的全部本地工具绑定。
     */
    void replaceBySkillId(String skillId, List<SkillLocalToolBinding> bindings);
}
