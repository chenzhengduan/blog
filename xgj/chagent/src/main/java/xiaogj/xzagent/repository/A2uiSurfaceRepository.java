package xiaogj.xzagent.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import xiaogj.xzagent.model.A2uiSurfaceState;

public interface A2uiSurfaceRepository {
    List<A2uiSurfaceState> findBySessionId(String sessionId);

    List<A2uiSurfaceState> findBySessionIdAndLastUpdatedMsgIds(String sessionId, Collection<String> messageIds);

    Set<String> findBoundMessageIds(String sessionId);

    void upsert(String sessionId, A2uiSurfaceState surface);

    void upsertAll(String sessionId, Collection<A2uiSurfaceState> surfaces);

    void updateLastUpdatedMsgId(String sessionId, Collection<String> surfaceIds, String messageId);

    void deleteBySessionIdAndSurfaceIds(String sessionId, Collection<String> surfaceIds);

    void deleteBySessionId(String sessionId);
}
