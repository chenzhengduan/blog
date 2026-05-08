package xiaogj.xzagent.service.hook;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreCallEvent;
import io.agentscope.core.hook.PreReasoningEvent;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.tool.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Xzagent 侧对 AgentScope AutoContextHook 的线程安全替代实现。
 *
 * <p>功能与 {@code io.agentscope.core.memory.autocontext.AutoContextHook} 完全等价：
 * <ol>
 *   <li>首次调用时向 Toolkit 注册 {@link ContextOffloadTool}。</li>
 *   <li>首次调用时将 {@link PlanNotebook}（如存在）绑定到 {@link AutoContextMemory}。</li>
 *   <li>每次 LLM 推理前触发 AutoContext 压缩，并向 System Prompt 注入压缩提示。</li>
 * </ol>
 *
 * <p><b>修复内容（相对于原始 AutoContextHook）：</b><br>
 * 原始实现在 {@code handlePreReasoning} 中同步调用 {@code compressIfNeeded()}，
 * 该方法内部包含多处 {@code .block()} 调用。当 {@link PreReasoningEvent} 在
 * Netty {@code reactor-http-epoll-*} 线程上触发时（例如远程工具 HTTP 响应返回后），
 * 会抛出 {@code block()/blockFirst()/blockLast() are blocking, which is not supported
 * in thread reactor-http-epoll-*}。
 *
 * <p>本实现通过将 {@code handlePreReasoning} 的执行体包装在
 * {@code Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())} 中，
 * 确保阻塞操作始终在 boundedElastic 线程池上执行，而非 Netty epoll 线程。
 *
 * <p><b>使用方式：</b>在 {@code AgentFactory} 中以本类替代原始 {@code AutoContextHook}：
 * <pre>{@code
 * .hooks(List.of(
 *     new XzagentAutoContextHook(),   // 替代 new AutoContextHook()
 *     surfaceTrackingHook,
 *     new ToolAuditHook(...)
 * ))
 * }</pre>
 *
 * @see io.agentscope.core.memory.autocontext.AutoContextHook 原始实现（不修改）
 * @see AutoContextMemory
 * @see ContextOffloadTool
 */
public class XzagentAutoContextHook implements Hook {

    private static final Logger log = LoggerFactory.getLogger(XzagentAutoContextHook.class);

    /**
     * 一次性初始化标志，保证 ContextOffloadTool 注册和 PlanNotebook 绑定仅执行一次。
     */
    private final AtomicBoolean registered = new AtomicBoolean(false);

    // -------------------------------------------------------------------------
    // Hook 接口实现
    // -------------------------------------------------------------------------

    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PreCallEvent preCallEvent) {
            @SuppressWarnings("unchecked")
            Mono<T> result = (Mono<T>) handlePreCall(preCallEvent);
            return result;
        }
        if (event instanceof PreReasoningEvent preReasoningEvent) {
            @SuppressWarnings("unchecked")
            Mono<T> result = (Mono<T>) handlePreReasoning(preReasoningEvent);
            return result;
        }
        return Mono.just(event);
    }

    /**
     * 优先级与原始 AutoContextHook 保持一致（高优先级 = 较小数值），
     * 确保在其他 Hook 处理事件之前完成 AutoContext 初始化和压缩。
     */
    @Override
    public int priority() {
        return 0;
    }

    // -------------------------------------------------------------------------
    // PreCallEvent：一次性初始化
    // -------------------------------------------------------------------------

    /**
     * 处理 {@link PreCallEvent}，完成 AutoContextMemory 与 Agent 的集成初始化。
     *
     * <p>仅当 Agent 为 {@link ReActAgent} 且 Memory 为 {@link AutoContextMemory} 时执行；
     * 通过 CAS 保证并发安全，整个生命周期内只执行一次。
     *
     * @param event PreCallEvent
     * @return 未修改的事件 Mono
     */
    private Mono<PreCallEvent> handlePreCall(PreCallEvent event) {
        // 已完成初始化则跳过
        if (registered.get()) {
            return Mono.just(event);
        }

        Agent agent = event.getAgent();

        // 只处理 ReActAgent
        if (!(agent instanceof ReActAgent reActAgent)) {
            return Mono.just(event);
        }

        // 只处理 AutoContextMemory
        Memory memory = reActAgent.getMemory();
        if (!(memory instanceof AutoContextMemory autoContextMemory)) {
            return Mono.just(event);
        }

        // CAS 保证只有一个线程执行初始化
        if (!registered.compareAndSet(false, true)) {
            return Mono.just(event);
        }

        try {
            // 向 Toolkit 注册 ContextOffloadTool，使 LLM 可以召回被卸载的上下文
            Toolkit toolkit = reActAgent.getToolkit();
            if (toolkit != null) {
                ContextOffloadTool contextOffloadTool = new ContextOffloadTool(autoContextMemory);
                toolkit.registerTool(contextOffloadTool);
                log.debug("ContextOffloadTool 已注册，agent={}", agent.getClass().getSimpleName());
            } else {
                log.warn("Toolkit 为空，无法注册 ContextOffloadTool");
            }

            // 若 Agent 启用了规划功能，将 PlanNotebook 绑定到 AutoContextMemory
            PlanNotebook planNotebook = reActAgent.getPlanNotebook();
            if (planNotebook != null) {
                autoContextMemory.attachPlanNote(planNotebook);
                log.debug("PlanNotebook 已绑定到 AutoContextMemory，agent={}", agent.getClass().getSimpleName());
            } else {
                log.debug("Agent 未启用 PlanNotebook，agent={}", agent.getClass().getSimpleName());
            }

            log.info("AutoContextMemory 集成初始化完成，agent={}", agent.getClass().getSimpleName());
        } catch (Exception e) {
            // 不中断主流程；重置标志以允许下次重试
            log.error("AutoContextMemory 集成初始化失败，agent={}", agent.getClass().getSimpleName(), e);
            registered.set(false);
        }

        return Mono.just(event);
    }

    // -------------------------------------------------------------------------
    // PreReasoningEvent：压缩 + 系统提示注入（线程安全版本）
    // -------------------------------------------------------------------------

    /**
     * 处理 {@link PreReasoningEvent}，在 LLM 推理前触发上下文压缩并注入系统提示指令。
     *
     * <p><b>线程安全修复：</b>通过 {@code Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())}
     * 将 {@code compressIfNeeded()} 的阻塞执行切换到 boundedElastic 线程池，
     * 避免在 Netty epoll 线程上调用 {@code .block()} 引发异常。
     *
     * <p>压缩完成后，将 AutoContextMemory 的最新消息（含压缩结果）设置为推理输入，
     * 并向 System Prompt 追加关于压缩消息格式的使用说明。
     *
     * @param event PreReasoningEvent（可变，setInputMessages 会就地更新）
     * @return 含更新后输入消息的事件 Mono（在 boundedElastic 线程上完成）
     */
    private Mono<PreReasoningEvent> handlePreReasoning(PreReasoningEvent event) {
        Agent agent = event.getAgent();

        // 只处理 ReActAgent
        if (!(agent instanceof ReActAgent reActAgent)) {
            return Mono.just(event);
        }

        // 只处理 AutoContextMemory
        Memory memory = reActAgent.getMemory();
        if (!(memory instanceof AutoContextMemory autoContextMemory)) {
            return Mono.just(event);
        }

        /*
         * 关键修复：将阻塞的 compressIfNeeded() 以及后续消息构建逻辑
         * 整体包装在 Mono.fromCallable + subscribeOn(boundedElastic) 中。
         * 框架通过 Mono 接口以 reactive 方式订阅本方法返回值，
         * 因此切换调度器后阻塞操作将在 boundedElastic 线程上执行，
         * 而非 Netty reactor-http-epoll-* 线程。
         */
        return Mono.fromCallable(() -> {
                    // 触发上下文压缩（内部包含 .block() 调用，必须在非 epoll 线程执行）
                    autoContextMemory.compressIfNeeded();

                    // 构建含压缩使用说明的新消息列表
                    List<Msg> originalInputMessages = event.getInputMessages();
                    List<Msg> newInputMessages = new ArrayList<>();

                    String appendedInstruction =
                            "\n\n"
                                    + "You may see compressed messages containing <!-- CONTEXT_OFFLOAD"
                                    + " uuid=... -->.\n"
                                    + "- Use the UUID to call context_reload if you need full details.\n"
                                    + "- NEVER mention, quote, or refer to UUIDs, offload tags, or internal"
                                    + " metadata in your response.";

                    if (!originalInputMessages.isEmpty()
                            && originalInputMessages.get(0).getRole() == MsgRole.SYSTEM) {
                        // 已存在 System 消息：追加压缩使用说明
                        Msg originalSystemMsg = originalInputMessages.get(0);
                        String originalSystemText = originalSystemMsg.getTextContent();
                        String newSystemText =
                                originalSystemText != null
                                        ? originalSystemText + appendedInstruction
                                        : appendedInstruction.trim();

                        Msg updatedSystemMsg =
                                Msg.builder()
                                        .role(MsgRole.SYSTEM)
                                        .name(originalSystemMsg.getName())
                                        .content(TextBlock.builder().text(newSystemText).build())
                                        .metadata(originalSystemMsg.getMetadata())
                                        .build();
                        newInputMessages.add(updatedSystemMsg);
                    } else {
                        // 不存在 System 消息：新建一条携带压缩使用说明的 System 消息
                        String instruction =
                                "You may see compressed messages containing <!-- CONTEXT_OFFLOAD uuid=..."
                                        + " -->.\n"
                                        + "- Use the UUID to call context_reload if you need full details.\n"
                                        + "- NEVER mention, quote, or refer to UUIDs, offload tags, or internal"
                                        + " metadata in your response.";

                        newInputMessages.add(
                                Msg.builder()
                                        .role(MsgRole.SYSTEM)
                                        .name("system")
                                        .content(TextBlock.builder().text(instruction).build())
                                        .build());
                    }

                    // 追加 AutoContextMemory 当前消息（已含压缩结果）
                    newInputMessages.addAll(autoContextMemory.getMessages());
                    event.setInputMessages(newInputMessages);

                    return event;
                })
                // 将阻塞调用切换到 boundedElastic 线程池，避免阻塞 Netty epoll 线程
                .subscribeOn(Schedulers.boundedElastic());
    }
}
