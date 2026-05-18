package xiaogj.xzagent.service;

public record SessionHistoryMessage(
        String messageId,
        String role,
        String content,
        int messageOrder) {
}
