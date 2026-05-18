package xiaogj.xzagent.web.dto;

import java.util.List;

/**
 * 会话信息响应体。
 *
 * @param sessionId 会话标识
 * @param activeSkills 当前已激活的技能列表
 * @param messages 当前页消息历史
 * @param totalCount 当前会话消息总数
 */
public record SessionResponse(
        String sessionId,
        List<String> activeSkills,
        List<SessionMessageResponse> messages,
        int totalCount) {
}
