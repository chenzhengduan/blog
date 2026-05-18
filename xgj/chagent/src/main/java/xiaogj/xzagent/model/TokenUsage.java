package xiaogj.xzagent.model;

/**
 * Token 消耗明细，对应 token_usage 表。
 *
 * <p>每次用户请求触发一次 agent.call()，产生一条记录。
 * 一次 call 内可能包含多轮 LLM 推理（ReAct 循环），
 * {@code inputTokens} / {@code outputTokens} 是该 call 内所有推理的累计值。
 *
 * @param userId       用户主键，来自认证上下文
 * @param sessionId    会话标识，关联 agent_session.session_id
 * @param runId        运行标识，关联 agent_run.run_id
 * @param agentId      Agent 标识，来自 agent_definition.agent_id
 * @param modelProvider 模型供应商（如 anthropic、openai）
 * @param modelName    模型名称（如 claude-3-5-sonnet-20241022）
 * @param inputTokens  本次 call 累计输入 token 数
 * @param outputTokens 本次 call 累计输出 token 数
 */
public record TokenUsage(
        Long userId,
        String sessionId,
        String runId,
        String agentId,
        String modelProvider,
        String modelName,
        long inputTokens,
        long outputTokens) {
}
