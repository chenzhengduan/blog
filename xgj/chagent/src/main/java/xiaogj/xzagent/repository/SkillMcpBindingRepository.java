package xiaogj.xzagent.repository;

import xiaogj.xzagent.model.SkillMcpBinding;
import java.util.List;

public interface SkillMcpBindingRepository {
    /**
     * 查询所有启用的 Skill-MCP 绑定配置。
     */
    List<SkillMcpBinding> findEnabledBindings();

    /**
     * 查询指定 MCP Server 的全部绑定配置。
     */
    List<SkillMcpBinding> findByServerId(String serverId);

    /**
     * 查询指定 Skill 的全部绑定配置。
     */
    List<SkillMcpBinding> findBySkillId(String skillId);

    /**
     * 使用给定绑定列表替换指定 server 的全部绑定。
     */
    void replaceByServerId(String serverId, List<SkillMcpBinding> bindings);

    /**
     * 使用给定绑定列表替换指定 Skill 的全部绑定。
     */
    void replaceBySkillId(String skillId, List<SkillMcpBinding> bindings);
}
