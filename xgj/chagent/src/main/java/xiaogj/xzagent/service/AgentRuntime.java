package xiaogj.xzagent.service;

import io.agentscope.core.ReActAgent;
import xiaogj.xzagent.service.hook.A2uiSurfaceTrackingHook;
import io.agentscope.core.session.Session;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.state.SimpleSessionKey;

/**
 * 一次请求对应的 Agent 运行时快照。
 *
 * @param sessionId 当前会话标识
 * @param runId 本次调用的运行标识
 * @param userId 当前登录用户主键
 * @param username 当前登录用户名，仅用于日志与诊断
 * @param agent 已装配完成的 Agent 实例
 * @param skillBox 当前会话使用的 SkillBox
 * @param session AgentScope 会话存储实现
 * @param sessionKey AgentScope 会话主键
 * @param a2uiSurfaceTrackingHook 当前请求绑定的 A2UI Surface 跟踪 Hook
 */
public record AgentRuntime(
        String sessionId,
        String runId,
        Long userId,
        String username,
        ReActAgent agent,
        SkillBox skillBox,
        Session session,
        SimpleSessionKey sessionKey,
        A2uiSurfaceTrackingHook a2uiSurfaceTrackingHook) {
}
