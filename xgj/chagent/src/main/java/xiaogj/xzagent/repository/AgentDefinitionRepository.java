package xiaogj.xzagent.repository;

import java.util.Optional;
import xiaogj.xzagent.model.AgentDefinition;

/**
 * Agent 定义仓储。
 */
public interface AgentDefinitionRepository {

    /**
     * 查询当前启用中的 Agent 定义。
     *
     * <p>当前系统约定同一时刻只存在一个启用 Agent，因此该方法返回单值。
     */
    Optional<AgentDefinition> findActive();

    /**
     * 保存或更新 Agent 定义。
     *
     * @param definition Agent 定义
     */
    void upsert(AgentDefinition definition);
}

