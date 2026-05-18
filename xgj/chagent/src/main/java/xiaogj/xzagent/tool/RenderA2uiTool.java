package xiaogj.xzagent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * 输出标准 A2UI message 的本地工具。
 *
 * <p>入参为结构化字段，callAsync 自动组装成 createSurface + updateComponents
 * （+ updateDataModel）三条 A2UI 消息序列。
 */
public class RenderA2uiTool implements AgentTool {

    public static final String TOOL_NAME = "render_a2ui";

    private static final String DESCRIPTION = """
            渲染一个 A2UI 动态界面（surface）。
            仅在需要让前端创建或更新交互界面时调用，不要返回解释性文本。

            支持的组件及关键字段（components 数组中每项）：
            - Column   : { id, component:"Column", children:[id,...] }
            - Row      : { id, component:"Row",    children:[id,...] }
            - Card     : { id, component:"Card",   children:[id,...] }
            - Divider  : { id, component:"Divider" }
            - Text     : { id, component:"Text",   text:"字面量" 或 {path:"/路径"}, variant?:"h1"|"h2"|"h3"|"body"|"caption" }
            - TextField: { id, component:"TextField", label:"标签", value:{path:"/路径"}, variant?:"shortText"|"longText"|"number" }
            - CheckBox : { id, component:"CheckBox",  label:"标签", value:{path:"/路径"} }
            - Button   : { id, component:"Button",    child:"文字节点id", variant?:"primary"|"borderless", action:{event:{name:"动作名", context?:{...}}} }

            规则：
            - 根组件 id 必须为 "root"
            - 所有 id 在同一 surface 内唯一
            - path 使用 JSON Pointer 格式，以 "/" 开头，对应 data 中的路径
            - children/child 引用其他组件的 id，不可循环
            """;

    private final ObjectMapper objectMapper;

    public RenderA2uiTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "surfaceId", Map.of(
                                "type", "string",
                                "description", "Surface 唯一标识符，同一会话内不同 surface 须不同"),
                        "catalogId", Map.of(
                                "type", "string",
                                "description", "组件 catalog，固定传 \"basic\""),
                        "components", Map.of(
                                "type", "array",
                                "description", "A2UI 组件数组（扁平结构），根组件 id 必须为 \"root\"；delete=true 时可省略",
                                "items", Map.of("type", "object", "additionalProperties", true)),
                        "data", Map.of(
                                "type", "object",
                                "description", "可选初始数据模型，供 path 绑定读取，如 {\"form\":{\"email\":\"\"}}",
                                "additionalProperties", true),
                        "delete", Map.of(
                                "type", "boolean",
                                "description", "是否删除指定 surface；为 true 时输出 deleteSurface 消息，不再输出 create/update 消息")),
                "required", List.of("surfaceId", "catalogId"),
                "additionalProperties", false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        Map<String, Object> input = param.getInput();
        String surfaceId = (String) input.get("surfaceId");
        String catalogId = (String) input.get("catalogId");
        List<Object> components = (List<Object>) input.get("components");
        Map<String, Object> data = (Map<String, Object>) input.get("data");
        boolean delete = Boolean.TRUE.equals(input.get("delete"));
        if (!delete && components == null) {
            return Mono.error(new IllegalArgumentException("A2UI 渲染缺少 components"));
        }

        List<Map<String, Object>> messages = buildMessages(surfaceId, catalogId, components, data, delete);
        try {
            String json = objectMapper.writeValueAsString(messages);
            return Mono.just(ToolResultBlock.of(TextBlock.builder().text(json).build()));
        } catch (JsonProcessingException error) {
            return Mono.error(new IllegalArgumentException("A2UI message 序列化失败", error));
        }
    }

    private List<Map<String, Object>> buildMessages(
            String surfaceId,
            String catalogId,
            List<Object> components,
            Map<String, Object> data,
            boolean delete) {
        List<Map<String, Object>> messages = new ArrayList<>();
        if (delete) {
            messages.add(Map.of(
                    "version", "v0.9",
                    "deleteSurface", Map.of("surfaceId", surfaceId)));
            return messages;
        }
        messages.add(Map.of(
                "version", "v0.9",
                "createSurface", Map.of("surfaceId", surfaceId, "catalogId", catalogId)));
        messages.add(Map.of(
                "version", "v0.9",
                "updateComponents", Map.of("surfaceId", surfaceId, "components", components != null ? components : List.of())));
        if (data != null && !data.isEmpty()) {
            messages.add(Map.of(
                    "version", "v0.9",
                    "updateDataModel", Map.of("surfaceId", surfaceId, "data", data)));
        }
        return messages;
    }
}
