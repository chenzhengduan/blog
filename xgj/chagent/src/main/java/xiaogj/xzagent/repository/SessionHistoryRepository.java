package xiaogj.xzagent.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.service.SessionHistoryMessage;
import xiaogj.xzagent.service.SessionHistoryPage;
import xiaogj.xzagent.service.SessionHistorySummary;

public interface SessionHistoryRepository {
    SessionHistoryPage findPage(String sessionId, int page, int size);

    Optional<SessionHistorySummary> findStoredSummary(String sessionId);

    SessionHistorySummary computeIndexSummary(String sessionId);

    void replaceVisibleMessages(String sessionId, List<SessionHistoryMessage> messages, Instant sourceUpdatedAt);

    /**
     * 在 Agent run 开始时，将本轮用户消息预先写入历史索引，以便重连时立即可见。
     * run 结束后 {@link #replaceVisibleMessages} 会覆盖此条记录，保证最终数据一致。
     *
     * @param sessionId 会话 ID
     * @param messageId 消息 ID（与前端一致）
     * @param content   消息文本内容
     */
    void appendPendingUserMessage(String sessionId, String messageId, String content);

    void deleteBySessionId(String sessionId);
}
