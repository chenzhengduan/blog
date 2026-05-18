package xiaogj.xzagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.agui.event.AguiEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.tool.RenderA2uiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 将 render_a2ui 工具结果转换为标准 AG-UI CUSTOM 事件。
 */
@Service
public class A2uiEventPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(A2uiEventPostProcessor.class);
    private static final String A2UI_EVENT_NAME = "a2ui";

    private final ObjectMapper objectMapper;

    public A2uiEventPostProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Flux<AguiEvent> process(Flux<AguiEvent> source) {
        Map<String, String> toolNamesByCallId = new HashMap<>();
        return source.concatMap(event -> Flux.fromIterable(processEvent(event, toolNamesByCallId)));
    }

    private List<AguiEvent> processEvent(AguiEvent event, Map<String, String> toolNamesByCallId) {
        if (event instanceof AguiEvent.ToolCallStart toolCallStart) {
            toolNamesByCallId.put(toolCallStart.toolCallId(), toolCallStart.toolCallName());
            return List.of(event);
        }
        if (event instanceof AguiEvent.ToolCallResult toolCallResult) {
            String toolName = toolNamesByCallId.remove(toolCallResult.toolCallId());
            if (RenderA2uiTool.TOOL_NAME.equals(toolName)) {
                return toA2uiCustomEvents(toolCallResult);
            }
            return List.of(event);
        }
        if (event instanceof AguiEvent.RunFinished) {
            toolNamesByCallId.clear();
        }
        return List.of(event);
    }

    private List<AguiEvent> toA2uiCustomEvents(AguiEvent.ToolCallResult toolCallResult) {
        String content = toolCallResult.content();
        if (content == null || content.isBlank()) {
            return List.of(toolCallResult);
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            if (root.isArray()) {
                List<AguiEvent> events = new ArrayList<>();
                for (JsonNode item : root) {
                    if (!item.isObject()) {
                        log.warn("忽略非对象 A2UI message: toolCallId={}", toolCallResult.toolCallId());
                        return List.of(toolCallResult);
                    }
                    events.add(new AguiEvent.Custom(
                            toolCallResult.threadId(),
                            toolCallResult.runId(),
                            A2UI_EVENT_NAME,
                            objectMapper.convertValue(item, Object.class)));
                }
                return events;
            }
            if (!root.isObject()) {
                log.warn("忽略非法 A2UI message 根节点: toolCallId={}", toolCallResult.toolCallId());
                return List.of(toolCallResult);
            }
            return List.of(new AguiEvent.Custom(
                    toolCallResult.threadId(),
                    toolCallResult.runId(),
                    A2UI_EVENT_NAME,
                    objectMapper.convertValue(root, Object.class)));
        } catch (JsonProcessingException error) {
            log.warn(
                    "解析 render_a2ui 结果失败，保留原始 ToolCallResult: toolCallId={}, message={}",
                    toolCallResult.toolCallId(),
                    error.getMessage());
            return List.of(toolCallResult);
        }
    }
}
