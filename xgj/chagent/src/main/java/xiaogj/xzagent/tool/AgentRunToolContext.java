package xiaogj.xzagent.tool;

/**
 * 工具执行时的运行上下文，通过 {@code ToolkitConfig.defaultContext} 在服务端注入。
 *
 * <p>该上下文专门承载一次 Agent 运行级别的信息，避免将运行标识混入用户身份上下文，
 * 便于工具按需记录审计、计费与用量数据，同时保持模块职责清晰。
 *
 * @param sessionId 当前会话 ID
 * @param runId 当前运行 ID
 */
public record AgentRunToolContext(String sessionId, String runId) {
}
