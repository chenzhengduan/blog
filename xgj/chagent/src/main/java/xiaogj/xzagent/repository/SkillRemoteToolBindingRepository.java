package xiaogj.xzagent.repository;

import java.util.List;
import xiaogj.xzagent.model.SkillRemoteToolBinding;

/**
 * Skill 与远程工具绑定仓储。
 */
public interface SkillRemoteToolBindingRepository {

    /**
     * 查询所有启用中的 Skill 远程工具绑定。
     *
     * @return 绑定列表
     */
    List<SkillRemoteToolBinding> findEnabledBindings();

    /**
     * 查询指定 Skill 的启用绑定。
     *
     * @param skillId Skill 逻辑标识
     * @return 该 Skill 对应的远程工具绑定
     */
    List<SkillRemoteToolBinding> findEnabledBindingsBySkillId(String skillId);

    /**
     * 使用新的绑定列表覆盖指定 source 的全部绑定。
     */
    void replaceBySourceId(String sourceId, List<SkillRemoteToolBinding> bindings);

    /**
     * 删除指定来源下的全部远程工具绑定。
     *
     * @param sourceId 来源唯一标识
     */
    void deleteBySourceId(String sourceId);

    /**
     * 使用新的绑定列表覆盖指定 Skill 的全部远程绑定。
     */
    void replaceBySkillId(String skillId, List<SkillRemoteToolBinding> bindings);
}
