package xiaogj.xzagent.service.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PostActingEvent;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import xiaogj.xzagent.repository.ToolAuditRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * 工具调用后置审计 Hook。
 */
public class ToolAuditHook implements Hook {

    private static final Logger log = LoggerFactory.getLogger(ToolAuditHook.class);

    private final String runId;
    private final ToolAuditRepository repository;
    private final Set<String> localToolNames;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolAuditHook(
            String runId,
            ToolAuditRepository repository,
            Set<String> localToolNames) {
        this.runId = runId;
        this.repository = repository;
        this.localToolNames = localToolNames;
    }

    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PostActingEvent postActingEvent) {
            ToolResultBlock toolResult = postActingEvent.getToolResult();
            String toolName = postActingEvent.getToolUse().getName();
            String argsJson = writeJson(postActingEvent.getToolUse().getInput());
            // 当前审计表先只落文本结果，后续如果要保留结构化输出，
            // 可以扩展为完整 block 列表序列化。
            String resultText = toolResult.getOutput().stream()
                    .filter(TextBlock.class::isInstance)
                    .map(TextBlock.class::cast)
                    .map(TextBlock::getText)
                    .reduce("", String::concat);
            String resultJson = writeJson(resultText);
            try {
                repository.saveAudit(
                        runId,
                        toolName,
                        argsJson,
                        resultJson,
                        "COMPLETED");
            } catch (Exception e) {
                // 审计写入失败不中断 Agent 主链路，仅记录日志供排查
                log.error("工具审计日志写入失败（不影响 Agent 运行）: runId={}, toolName={}, error={}",
                        runId, toolName, e.getMessage(), e);
            }
            if (localToolNames.contains(toolName)) {
                log.info(
                        "本地工具原始调用: runId={}, toolName={}, rawArgs={}, rawResult={}",
                        runId,
                        toolName,
                        argsJson,
                        resultJson);
            }
        }
        return Mono.just(event);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            // 审计逻辑不能反向影响主链路，因此这里退化为固定字符串。
            return "\"serialization_error\"";
        }
    }
}
