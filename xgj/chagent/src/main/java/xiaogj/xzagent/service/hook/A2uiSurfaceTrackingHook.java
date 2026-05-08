package xiaogj.xzagent.service.hook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PostActingEvent;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.state.SimpleSessionKey;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import xiaogj.xzagent.infrastructure.session.MysqlAgentSession;
import xiaogj.xzagent.model.A2uiSurfaceState;
import xiaogj.xzagent.repository.A2uiSurfaceRepository;
import xiaogj.xzagent.tool.RenderA2uiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class A2uiSurfaceTrackingHook implements Hook {

    private static final Logger log = LoggerFactory.getLogger(A2uiSurfaceTrackingHook.class);
    private static final String SESSION_KEY = "a2ui_surfaces";

    private final MysqlAgentSession session;
    private final SimpleSessionKey sessionKey;
    private final ObjectMapper objectMapper;
    private final A2uiSurfaceRepository a2uiSurfaceRepository;
    private final Map<String, A2uiSurfaceState> surfaces = new LinkedHashMap<>();
    private final Set<String> dirtySurfaceIds = new LinkedHashSet<>();

    public A2uiSurfaceTrackingHook(
            MysqlAgentSession session,
            SimpleSessionKey sessionKey,
            ObjectMapper objectMapper,
            A2uiSurfaceRepository a2uiSurfaceRepository) {
        this.session = session;
        this.sessionKey = sessionKey;
        this.objectMapper = objectMapper;
        this.a2uiSurfaceRepository = a2uiSurfaceRepository;
        loadFromStorage();
    }

    public List<A2uiSurfaceState> getSurfaces() {
        return new ArrayList<>(surfaces.values());
    }

    public Set<String> getDirtySurfaceIds() {
        return Set.copyOf(dirtySurfaceIds);
    }

    public void clearDirtySurfaceIds() {
        dirtySurfaceIds.clear();
    }

    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PostActingEvent postActingEvent
                && RenderA2uiTool.TOOL_NAME.equals(postActingEvent.getToolUse().getName())) {
            String resultText = extractText(postActingEvent.getToolResult());
            applyMessages(resultText);
            persist();
        }
        return Mono.just(event);
    }

    private void loadFromStorage() {
        String sessionId = sessionKey.toIdentifier();
        List<A2uiSurfaceState> persisted = a2uiSurfaceRepository.findBySessionId(sessionId);
        if (!persisted.isEmpty()) {
            persisted.forEach(surface -> surfaces.put(surface.surfaceId(), surface));
            return;
        }
        List<A2uiSurfaceState> legacy = session.getList(sessionKey, SESSION_KEY, A2uiSurfaceState.class);
        legacy.forEach(surface -> surfaces.put(surface.surfaceId(), surface));
        if (!legacy.isEmpty()) {
            a2uiSurfaceRepository.upsertAll(sessionId, legacy);
        }
    }

    private void applyMessages(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                return;
            }
            for (JsonNode msg : root) {
                if (msg.has("createSurface")) {
                    JsonNode cs = msg.get("createSurface");
                    String surfaceId = cs.get("surfaceId").asText();
                    String catalogId = cs.has("catalogId") ? cs.get("catalogId").asText() : "basic";
                    dirtySurfaceIds.add(surfaceId);
                    surfaces.put(surfaceId, new A2uiSurfaceState(surfaceId, catalogId, List.of(), Map.of(), null));
                }
                if (msg.has("updateComponents")) {
                    JsonNode uc = msg.get("updateComponents");
                    String surfaceId = uc.get("surfaceId").asText();
                    List<Object> components = objectMapper.convertValue(
                            uc.get("components"), new TypeReference<>() {});
                    A2uiSurfaceState current = surfaces.getOrDefault(
                            surfaceId, new A2uiSurfaceState(surfaceId, "basic", List.of(), Map.of(), null));
                    dirtySurfaceIds.add(surfaceId);
                    surfaces.put(surfaceId, new A2uiSurfaceState(
                            surfaceId,
                            current.catalogId(),
                            components,
                            current.dataModel(),
                            current.lastUpdatedMsgId()));
                }
                if (msg.has("updateDataModel")) {
                    JsonNode udm = msg.get("updateDataModel");
                    String surfaceId = udm.get("surfaceId").asText();
                    Map<String, Object> newData = objectMapper.convertValue(
                            udm.get("data"), new TypeReference<>() {});
                    A2uiSurfaceState current = surfaces.getOrDefault(
                            surfaceId, new A2uiSurfaceState(surfaceId, "basic", List.of(), Map.of(), null));
                    Map<String, Object> merged = new LinkedHashMap<>(current.dataModel());
                    merged.putAll(newData);
                    dirtySurfaceIds.add(surfaceId);
                    surfaces.put(surfaceId, new A2uiSurfaceState(
                            surfaceId,
                            current.catalogId(),
                            current.components(),
                            merged,
                            current.lastUpdatedMsgId()));
                }
                if (msg.has("deleteSurface")) {
                    String surfaceId = msg.get("deleteSurface").get("surfaceId").asText();
                    dirtySurfaceIds.add(surfaceId);
                    surfaces.remove(surfaceId);
                }
            }
        } catch (Exception e) {
            log.warn("A2UI surface 状态更新失败: {}", e.getMessage());
        }
    }

    private void persist() {
        if (dirtySurfaceIds.isEmpty()) {
            return;
        }
        String sessionId = sessionKey.toIdentifier();
        List<A2uiSurfaceState> upserts = dirtySurfaceIds.stream()
                .map(surfaces::get)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!upserts.isEmpty()) {
            a2uiSurfaceRepository.upsertAll(sessionId, upserts);
        }
        List<String> deleted = dirtySurfaceIds.stream()
                .filter(surfaceId -> !surfaces.containsKey(surfaceId))
                .toList();
        if (!deleted.isEmpty()) {
            a2uiSurfaceRepository.deleteBySessionIdAndSurfaceIds(sessionId, deleted);
        }
    }

    private String extractText(ToolResultBlock toolResult) {
        return toolResult.getOutput().stream()
                .filter(TextBlock.class::isInstance)
                .map(TextBlock.class::cast)
                .map(TextBlock::getText)
                .reduce("", String::concat);
    }
}
