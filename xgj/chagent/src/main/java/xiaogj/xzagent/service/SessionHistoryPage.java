package xiaogj.xzagent.service;

import java.util.List;

public record SessionHistoryPage(
        List<SessionHistoryMessage> messages,
        int totalCount) {
}
