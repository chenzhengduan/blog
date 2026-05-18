insert ignore into agent_definition(
    agent_id,
    name,
    system_prompt,
    max_iterations,
    enabled,
    created_at,
    updated_at
) values (
    'default',
    'xzagent-v1',
    '你是AI助手。
优先使用 Skill 元数据判断是否需要加载 Skill。
如果未加载相关 Skill，不要臆造工具能力。
只使用当前会话中可见的 Tool 和 MCP Tool。',
    12,
    true,
    current_timestamp,
    current_timestamp
);
