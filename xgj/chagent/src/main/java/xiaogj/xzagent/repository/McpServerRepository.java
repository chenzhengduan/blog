package xiaogj.xzagent.repository;

import xiaogj.xzagent.model.McpServerDefinition;
import java.util.List;

public interface McpServerRepository {
    /**
     * 查询当前处于启用状态的 MCP Server 定义。
     */
    List<McpServerDefinition> findEnabledServers();

    /**
     * 查询全部 MCP Server 定义。
     */
    List<McpServerDefinition> findAll();

    /**
     * 保存或更新 MCP Server 定义。
     */
    void upsert(McpServerDefinition definition);
}
