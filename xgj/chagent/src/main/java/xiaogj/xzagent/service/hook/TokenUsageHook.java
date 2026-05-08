package xiaogj.xzagent.service.hook;

import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PostCallEvent;
import io.agentscope.core.hook.PostReasoningEvent;
import io.agentscope.core.model.ChatUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import xiaogj.xzagent.model.TokenUsage;
import xiaogj.xzagent.repository.TokenUsageRepository;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Token 消耗统计 Hook。
 *
 * <p>一次 {@code agent.call()} 对应一个 Hook 实例（在 AgentFactory 中创建），因此可以安全地在实例内累计状态。
 *
 * <p>工作流程：
 * <ol>
 *     <li>{@link PostReasoningEvent}：每轮 LLM 推理完成后触发，从推理消息中读取 {@link ChatUsage} 并累加。
 *         ReAct 循环可能有多轮推理，因此需要在此事件中持续累计。</li>
 *     <li>{@link PostCallEvent}：整个 agent.call() 完成后触发，将累计值一次性持久化。
 *         写入失败仅记录日志，不中断主链路。</li>
 * </ol>
 */
public class TokenUsageHook implements Hook {

    private static final Logger log = LoggerFactory.getLogger(TokenUsageHook.class);

    /** 一次 call 内所有推理步骤的输入 token 累计值。 */
    private final AtomicLong accumulatedInput = new AtomicLong(0);
    /** 一次 call 内所有推理步骤的输出 token 累计值。 */
    private final AtomicLong accumulatedOutput = new AtomicLong(0);

    private final String runId;
    private final Long userId;
    private final String sessionId;
    private final String agentId;
    private final String modelProvider;
    private final String modelName;
    private final TokenUsageRepository repository;

    /**
     * @param runId          本次运行标识，关联 agent_run.run_id
     * @param userId         当前用户主键
     * @param sessionId      当前会话标识
     * @param agentId        Agent 标识，来自 agent_definition.agent_id
     * @param modelProvider  模型供应商（如 anthropic、openai）
     * @param modelName      模型名称（如具体型号）
     * @param repository     token 消耗持久化接口
     */
    public TokenUsageHook(
            String runId,
            Long userId,
            String sessionId,
            String agentId,
            String modelProvider,
            String modelName,
            TokenUsageRepository repository) {
        this.runId = runId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.agentId = agentId;
        this.modelProvider = modelProvider;
        this.modelName = modelName;
        this.repository = repository;
    }

    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PostReasoningEvent postReasoningEvent) {
            // 每轮 LLM 推理完成后累加本轮 token 消耗
            ChatUsage usage = postReasoningEvent.getReasoningMessage().getChatUsage();
            if (usage != null) {
                accumulatedInput.addAndGet(usage.getInputTokens());
                accumulatedOutput.addAndGet(usage.getOutputTokens());
                log.debug("本轮推理 token: input={}, output={}, 累计: input={}, output={}, runId={}",
                        usage.getInputTokens(), usage.getOutputTokens(),
                        accumulatedInput.get(), accumulatedOutput.get(), runId);
            }
        } else if (event instanceof PostCallEvent) {
            // 整个 call 完成，将累计 token 持久化
            long totalInput = accumulatedInput.get();
            long totalOutput = accumulatedOutput.get();
            // 无论 token 是否为 0 都打印，用于诊断框架是否正确上报 ChatUsage
            log.info("[TokenDiag] PostCallEvent fired: runId={}, input={}, output={}",
                    runId, totalInput, totalOutput);
            if (totalInput > 0 || totalOutput > 0) {
                TokenUsage tokenUsage = new TokenUsage(
                        userId, sessionId, runId, agentId,
                        modelProvider, modelName,
                        totalInput, totalOutput);
                repository.save(tokenUsage);
                log.info("token 消耗已记录: runId={}, input={}, output={}", runId, totalInput, totalOutput);
            } else {
                log.warn("[TokenDiag] token 均为 0，模型可能未上报 usage: runId={}, provider={}, model={}",
                        runId, modelProvider, modelName);
            }
        }
        return Mono.just(event);
    }

    /**
     * 低优先级执行，在业务逻辑 Hook 之后统计，不干扰主链路。
     */
    @Override
    public int priority() {
        return 500;
    }
}
