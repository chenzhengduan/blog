package xiaogj.xzagent.service;

import java.time.Instant;

public record SessionHistorySummary(
        int totalCount,
        Integer maxMessageOrder,
        String lastMessageId,
        long checksumSum,
        long checksumXor,
        Instant sourceUpdatedAt) {

    public boolean structurallyMatches(SessionHistorySummary other) {
        if (other == null) {
            return false;
        }
        return totalCount == other.totalCount
                && java.util.Objects.equals(maxMessageOrder, other.maxMessageOrder)
                && java.util.Objects.equals(lastMessageId, other.lastMessageId)
                && checksumSum == other.checksumSum
                && checksumXor == other.checksumXor;
    }
}
