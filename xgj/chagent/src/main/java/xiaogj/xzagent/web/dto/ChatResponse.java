package xiaogj.xzagent.web.dto;

/**
 * 同步聊天接口响应体。
 *
 * @param sessionId 会话标识
 * @param content 模型返回文本
 */
public record ChatResponse(String sessionId, String content) {
}
