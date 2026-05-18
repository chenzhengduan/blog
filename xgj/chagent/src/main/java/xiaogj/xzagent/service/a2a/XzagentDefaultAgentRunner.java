package xiaogj.xzagent.service.a2a;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.a2a.server.executor.runner.AgentRequestOptions;
import io.agentscope.core.a2a.server.executor.runner.AgentRunner;
import io.agentscope.core.agent.Event;
import io.agentscope.core.message.Msg;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import xiaogj.xzagent.model.AgentDefinition;
import xiaogj.xzagent.service.AgentDefinitionService;
import xiaogj.xzagent.service.AgentFactory;
import xiaogj.xzagent.service.AgentRunFinalizationService;
import xiaogj.xzagent.service.AgentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Xzagent 默认 Agent 的 A2A Runner。
 *
 * <p>这个 Runner 不重新实现 Agent 运行时，而是直接复用现有
 * {@link AgentFactory} 创建默认 agent。这样 A2A 与现有聊天接口能够共享：
 * <ul>
 *     <li>Skill / MCP / 远程工具绑定逻辑</li>
 *     <li>MySQL Session 持久化</li>
 *     <li>Tool 审计与运行状态记录</li>
 * </ul>
 *
 * <p>当前策略是“一次 A2A task 对应一个 AgentRuntime”，task 结束后清理缓存。
 */
@Component
public class XzagentDefaultAgentRunner implements AgentRunner {

    private static final Logger log =
            LoggerFactory.getLogger(XzagentDefaultAgentRunner.class);

    private final AgentFactory agentFactory;
    private final AgentDefinitionService agentDefinitionService;
    private final AgentRunFinalizationService agentRunFinalizationService;
    private final Map<String, AgentRuntime> runtimeCache = new ConcurrentHashMap<>();
    private final Set<String> stoppedTaskIds = ConcurrentHashMap.newKeySet();

    public XzagentDefaultAgentRunner(
            AgentFactory agentFactory,
            AgentDefinitionService agentDefinitionService,
            AgentRunFinalizationService agentRunFinalizationService) {
        this.agentFactory = agentFactory;
        this.agentDefinitionService = agentDefinitionService;
        this.agentRunFinalizationService = agentRunFinalizationService;
    }

    /**
     * 返回默认 agent 名称，供 A2A Server 构建基础 AgentCard。
     */
    @Override
    public String getAgentName() {
        return agentDefinitionService.getRuntimeDefinition().name();
    }

    /**
     * 返回默认 agent 描述。
     *
     * <p>当前 AgentDefinition 尚未单独建模 description，因此这里返回一个稳定描述，
     * 避免对外卡片为空。更详细描述由 A2A 配置中的 AgentCard description 提供。
     */
    @Override
    public String getAgentDescription() {
        AgentDefinition definition = agentDefinitionService.getRuntimeDefinition();
        return definition.name() + " 的 A2A 对外服务";
    }

    /**
     * 使用默认 agent 处理一次 A2A 请求。
     *
     * <p>会话主键优先取 A2A 请求中的 sessionId；若调用方未传，则退化为 taskId，
     * 确保每次 A2A 调用至少拥有稳定的 AgentScope Session 主键。
     */
    @Override
    public Flux<Event> stream(List<Msg> requestMessages, AgentRequestOptions options) {
        String taskId = options.getTaskId();
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("A2A 请求缺少 taskId。");
        }
        if (runtimeCache.containsKey(taskId)) {
            throw new IllegalStateException("A2A task 已存在运行中的 AgentRuntime: " + taskId);
        }

        String sessionId =
                options.getSessionId() != null && !options.getSessionId().isBlank()
                        ? options.getSessionId()
                        : taskId;
        Long userId = parseRequiredUserId(options.getUserId());
        AgentRuntime runtime = agentFactory.create(sessionId, taskId, userId, null);
        runtimeCache.put(taskId, runtime);
        log.info(
                "开始处理 A2A 请求: taskId={}, sessionId={}, userId={}, messageCount={}",
                taskId,
                runtime.sessionId(),
                userId,
                requestMessages != null ? requestMessages.size() : 0);

        // 直接复用 AgentScope 的流式事件输出，让 A2A Server 继续负责协议转换。
        return runtime.agent()
                .stream(requestMessages)
                .doOnComplete(() -> {
                    if (stoppedTaskIds.remove(taskId)) {
                        agentRunFinalizationService.stopA2a(runtime, taskId);
                        log.info(
                                "A2A 请求停止: taskId={}, sessionId={}, userId={}",
                                taskId,
                                runtime.sessionId(),
                                runtime.userId());
                        return;
                    }
                    agentRunFinalizationService.complete("a2a", runtime, taskId, "A2A_STREAM_COMPLETED");
                    log.info(
                            "A2A 请求完成: taskId={}, sessionId={}, userId={}",
                            taskId,
                            runtime.sessionId(),
                            runtime.userId());
                })
                .doOnError(error -> {
                    if (stoppedTaskIds.remove(taskId)) {
                        agentRunFinalizationService.stopA2a(runtime, taskId);
                        log.info(
                                "A2A 请求停止后结束: taskId={}, sessionId={}, userId={}, signal={}",
                                taskId,
                                runtime.sessionId(),
                                runtime.userId(),
                                error.getClass().getSimpleName());
                        return;
                    }
                    agentRunFinalizationService.fail("a2a", runtime, taskId, error);
                    log.error(
                            "A2A 请求失败: taskId={}, sessionId={}, userId={}, errorType={}, message={}",
                            taskId,
                            runtime.sessionId(),
                            runtime.userId(),
                            error.getClass().getSimpleName(),
                            error.getMessage(),
                            error);
                })
                .doFinally(signal -> {
                    stoppedTaskIds.remove(taskId);
                    runtimeCache.remove(taskId);
                });
    }

    /**
     * 停止指定 task 对应的 Agent。
     *
     * <p>A2A 协议允许客户端中断长任务。这里直接复用 AgentScope 的 interrupt 能力。
     */
    @Override
    public void stop(String taskId) {
        AgentRuntime runtime = runtimeCache.get(taskId);
        if (runtime == null || !stoppedTaskIds.add(taskId)) {
            return;
        }
        ReActAgent agent = runtime.agent();
        log.info(
                "收到 A2A 停止请求: taskId={}, sessionId={}, userId={}, username={}",
                taskId,
                runtime.sessionId(),
                runtime.userId(),
                runtime.username());
        agent.interrupt();
    }

    /**
     * A2A 层当前只会把服务端认证后的 `userId` 注入到请求元数据。
     *
     * <p>这里显式要求它必须是可解析的数字字符串，避免后续运行时再次退回
     * 到 `username` 之类不稳定身份键。
     */
    private Long parseRequiredUserId(String rawUserId) {
        if (rawUserId == null || rawUserId.isBlank()) {
            throw new IllegalArgumentException("A2A 请求缺少认证 userId。");
        }
        try {
            return Long.parseLong(rawUserId.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("A2A 请求 userId 不是合法数字: " + rawUserId, exception);
        }
    }
}
