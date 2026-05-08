package xiaogj.xzagent.web.dto;

import java.util.List;
import xiaogj.xzagent.model.A2uiSurfaceState;

/**
 * 会话消息响应体。
 *
 * @param id 消息唯一标识
 * @param role 消息角色，当前只返回 user 或 assistant
 * @param content 消息文本内容
 * @param surfaces 绑定到该消息的 A2UI Surface 列表
 */
public record SessionMessageResponse(
        String id,
        String role,
        String content,
        List<A2uiSurfaceState> surfaces) {
}
