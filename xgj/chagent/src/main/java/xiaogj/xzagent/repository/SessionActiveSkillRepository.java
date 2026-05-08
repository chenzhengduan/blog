package xiaogj.xzagent.repository;

import java.util.List;

public interface SessionActiveSkillRepository {
    /**
     * 查询会话内已激活的技能列表。
     */
    List<String> findActiveSkillIds(String sessionId);

    /**
     * 全量替换某个会话的激活技能集合。
     */
    void replaceActiveSkills(String sessionId, List<String> skillIds);

    /**
     * 清理会话的所有技能激活状态。
     */
    void deleteBySessionId(String sessionId);
}
