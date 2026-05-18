package xiaogj.xzagent.repository.jdbc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.repository.SessionHistoryRepository;
import xiaogj.xzagent.service.SessionHistoryMessage;
import xiaogj.xzagent.service.SessionHistoryPage;
import xiaogj.xzagent.service.SessionHistorySummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSessionHistoryRepository implements SessionHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSessionHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SessionHistoryPage findPage(String sessionId, int page, int size) {
        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from session_message_index where session_id = ?",
                Integer.class,
                sessionId);
        int totalCount = total != null ? total : 0;
        List<SessionHistoryMessage> messages = jdbcTemplate.query(
                """
                select message_id, role, content, message_order
                from session_message_index
                where session_id = ?
                order by message_order desc
                limit ? offset ?
                """,
                (rs, rowNum) -> new SessionHistoryMessage(
                        rs.getString("message_id"),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getInt("message_order")),
                sessionId,
                size,
                (Math.max(page, 1) - 1) * size);
        return new SessionHistoryPage(messages, totalCount);
    }

    @Override
    public Optional<SessionHistorySummary> findStoredSummary(String sessionId) {
        List<SessionHistorySummary> results = jdbcTemplate.query(
                """
                select total_count, max_message_order, last_message_id, checksum_sum, checksum_xor, source_updated_at
                from session_history_meta
                where session_id = ?
                """,
                (rs, rowNum) -> new SessionHistorySummary(
                        rs.getInt("total_count"),
                        rs.getObject("max_message_order") != null ? rs.getInt("max_message_order") : null,
                        rs.getString("last_message_id"),
                        rs.getLong("checksum_sum"),
                        rs.getLong("checksum_xor"),
                        rs.getTimestamp("source_updated_at") != null
                                ? rs.getTimestamp("source_updated_at").toInstant()
                                : null),
                sessionId);
        return results.stream().findFirst();
    }

    @Override
    public SessionHistorySummary computeIndexSummary(String sessionId) {
        IndexAggregate aggregate = jdbcTemplate.queryForObject(
                """
                select count(*) as total_count,
                       max(message_order) as max_message_order,
                       coalesce(sum(cast(crc32(concat_ws('|', message_order, message_id, role, coalesce(content, ''))) as unsigned)), 0) as checksum_sum,
                       coalesce(bit_xor(cast(crc32(concat_ws('|', message_order, message_id, role, coalesce(content, ''))) as unsigned)), 0) as checksum_xor
                from session_message_index
                where session_id = ?
                """,
                (rs, rowNum) -> new IndexAggregate(
                        rs.getInt("total_count"),
                        rs.getObject("max_message_order") != null ? rs.getInt("max_message_order") : null,
                        rs.getLong("checksum_sum"),
                        rs.getLong("checksum_xor")),
                sessionId);
        List<String> lastMessageIds = jdbcTemplate.query(
                """
                select message_id
                from session_message_index
                where session_id = ?
                order by message_order desc
                limit 1
                """,
                (rs, rowNum) -> rs.getString("message_id"),
                sessionId);
        String lastMessageId = lastMessageIds.stream().findFirst().orElse(null);
        if (aggregate == null) {
            return new SessionHistorySummary(0, null, null, 0L, 0L, null);
        }
        return new SessionHistorySummary(
                aggregate.totalCount(),
                aggregate.maxMessageOrder(),
                lastMessageId,
                aggregate.checksumSum(),
                aggregate.checksumXor(),
                null);
    }

    @Override
    public void replaceVisibleMessages(String sessionId, List<SessionHistoryMessage> messages, Instant sourceUpdatedAt) {
        deleteBySessionId(sessionId);
        if (messages != null && !messages.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    """
                    insert into session_message_index(session_id, message_id, role, content, message_order, updated_at)
                    values (?, ?, ?, ?, ?, current_timestamp)
                    """,
                    messages,
                    messages.size(),
                    (ps, message) -> {
                        ps.setString(1, sessionId);
                        ps.setString(2, message.messageId());
                        ps.setString(3, message.role());
                        ps.setString(4, message.content());
                        ps.setInt(5, message.messageOrder());
                    });
        }
        SessionHistorySummary summary = summarize(messages, sourceUpdatedAt);
        jdbcTemplate.update(
                """
                insert into session_history_meta(
                    session_id,
                    total_count,
                    max_message_order,
                    last_message_id,
                    checksum_sum,
                    checksum_xor,
                    source_updated_at,
                    updated_at)
                values (?, ?, ?, ?, ?, ?, ?, current_timestamp)
                on duplicate key update
                    total_count = values(total_count),
                    max_message_order = values(max_message_order),
                    last_message_id = values(last_message_id),
                    checksum_sum = values(checksum_sum),
                    checksum_xor = values(checksum_xor),
                    source_updated_at = values(source_updated_at),
                    updated_at = values(updated_at)
                """,
                sessionId,
                summary.totalCount(),
                summary.maxMessageOrder(),
                summary.lastMessageId(),
                summary.checksumSum(),
                summary.checksumXor(),
                sourceUpdatedAt != null ? java.sql.Timestamp.from(sourceUpdatedAt) : null);
    }

    @Override
    public void appendPendingUserMessage(String sessionId, String messageId, String content) {
        // 查询当前最大 message_order，用于确定新消息的排序位置
        Integer maxOrder = jdbcTemplate.queryForObject(
                "select max(message_order) from session_message_index where session_id = ?",
                Integer.class,
                sessionId);
        int nextOrder = maxOrder != null ? maxOrder + 1 : 0;
        // 若同 message_id 已存在（run 结束后 replaceVisibleMessages 已写入），则忽略
        jdbcTemplate.update(
                """
                insert ignore into session_message_index(session_id, message_id, role, content, message_order, updated_at)
                values (?, ?, 'user', ?, ?, current_timestamp)
                """,
                sessionId, messageId, content, nextOrder);
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        jdbcTemplate.update("delete from session_message_index where session_id = ?", sessionId);
        jdbcTemplate.update("delete from session_history_meta where session_id = ?", sessionId);
    }

    private SessionHistorySummary summarize(List<SessionHistoryMessage> messages, Instant sourceUpdatedAt) {
        if (messages == null || messages.isEmpty()) {
            return new SessionHistorySummary(0, null, null, 0L, 0L, sourceUpdatedAt);
        }
        long checksumSum = 0L;
        long checksumXor = 0L;
        for (SessionHistoryMessage message : messages) {
            long value = checksum(message);
            checksumSum += value;
            checksumXor ^= value;
        }
        SessionHistoryMessage last = messages.get(messages.size() - 1);
        return new SessionHistorySummary(
                messages.size(),
                last.messageOrder(),
                last.messageId(),
                checksumSum,
                checksumXor,
                sourceUpdatedAt);
    }

    private long checksum(SessionHistoryMessage message) {
        java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
        String raw = String.join("|",
                String.valueOf(message.messageOrder()),
                message.messageId() != null ? message.messageId() : "",
                message.role() != null ? message.role() : "",
                message.content() != null ? message.content() : "");
        crc32.update(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return crc32.getValue();
    }

    private record IndexAggregate(
            int totalCount,
            Integer maxMessageOrder,
            long checksumSum,
            long checksumXor) {
    }
}
