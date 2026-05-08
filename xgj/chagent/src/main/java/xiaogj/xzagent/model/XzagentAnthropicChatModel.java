package xiaogj.xzagent.model;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.RawMessageStreamEvent;
import io.agentscope.core.formatter.anthropic.AnthropicBaseFormatter;
import io.agentscope.core.formatter.anthropic.AnthropicChatFormatter;
import io.agentscope.core.formatter.anthropic.AnthropicResponseParser;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.ToolSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义 Anthropic Chat Model，修复 AgentScope streaming 模式下 token 消耗无法上报的问题。
 *
 * <p><b>问题根源</b>：{@code AnthropicResponseParser.parseStreamEvents()} 的 filter 条件
 * {@code !response.getContent().isEmpty()} 会过滤掉携带 token usage 的 {@code message_delta}
 * 事件——该事件只有 usage 数据，content 为空。导致 {@code ReasoningContext} 永远收不到 usage，
 * {@code PostReasoningEvent} 中 {@code getChatUsage()} 返回 null。
 *
 * <p><b>解决方案</b>：在应用层重新实现 streaming 事件解析：
 * <ul>
 *   <li>从 {@code message_start} 事件缓存 {@code inputTokens}（不直接发出，防止被后续事件覆盖）</li>
 *   <li>从 {@code message_delta} 事件提取 {@code outputTokens}，与缓存的 {@code inputTokens}
 *       合并，发出一个携带完整 token 统计的 {@link ChatResponse}</li>
 *   <li>filter 条件放宽为：content 不为空 OR usage 不为 null</li>
 * </ul>
 *
 * <p>非 streaming 路径使用 {@link AnthropicResponseParser#parseMessage}，原生支持 token 上报。
 * content streaming 逻辑（text/tool 块处理）与框架原始实现保持完全兼容。
 */
public class XzagentAnthropicChatModel extends ChatModelBase {

    private static final Logger log = LoggerFactory.getLogger(XzagentAnthropicChatModel.class);

    /** 模型名称，如 claude-opus-4-5。 */
    private final String modelName;

    /** 是否启用 streaming 模式。 */
    private final boolean streamEnabled;

    /** Anthropic Java SDK 客户端，支持 streaming 和非 streaming 调用。 */
    private final AnthropicClient client;

    /**
     * 消息格式化器，将 AgentScope {@link Msg} 列表转换为 Anthropic API 参数，
     * 并注入 system prompt、generation options、tools。
     */
    private final AnthropicBaseFormatter formatter;

    /** 默认生成选项，在调用方未传入 {@link GenerateOptions} 时生效。 */
    private final GenerateOptions defaultOptions;

    private XzagentAnthropicChatModel(
            String modelName,
            boolean streamEnabled,
            AnthropicClient client,
            AnthropicBaseFormatter formatter,
            GenerateOptions defaultOptions) {
        this.modelName = modelName;
        this.streamEnabled = streamEnabled;
        this.client = client;
        this.formatter = formatter;
        this.defaultOptions = defaultOptions;
    }

    /**
     * 返回模型名称，供 AgentScope 框架日志、hook 及指标上报使用。
     */
    @Override
    public String getModelName() {
        return modelName;
    }

    /**
     * 执行模型推理，由基类 {@code stream()} 统一调用。
     *
     * <p>streaming 路径使用 {@link #parseStreamEventsWithUsage} 正确捕获 token 消耗；
     * 非 streaming 路径使用 {@link AnthropicResponseParser#parseMessage}，原生支持 token 上报。
     *
     * @param messages 当前对话的消息列表
     * @param tools    可用工具列表，为空时不注入
     * @param options  本次调用的生成选项，null 时使用 defaultOptions
     * @return 包含 {@link ChatResponse} 的 Flux 流
     */
    @Override
    protected Flux<ChatResponse> doStream(
            List<Msg> messages, List<ToolSchema> tools, GenerateOptions options) {
        Instant startTime = Instant.now();
        log.debug("Anthropic doStream: model={}, messages={}, streaming={}",
                modelName, messages != null ? messages.size() : 0, streamEnabled);

        return Flux.defer(() -> {
            try {
                // 构建 Anthropic API 请求参数：系统提示、消息历史、生成选项、工具列表
                MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
                        .model(modelName)
                        .maxTokens(4096);

                formatter.applySystemMessage(paramsBuilder, messages);

                @SuppressWarnings("unchecked")
                List<MessageParam> formattedMessages =
                        (List<MessageParam>) formatter.format(messages);
                for (MessageParam param : formattedMessages) {
                    paramsBuilder.addMessage(param);
                }

                formatter.applyOptions(paramsBuilder, options, defaultOptions);

                if (tools != null && !tools.isEmpty()) {
                    formatter.applyTools(paramsBuilder, tools);
                }

                MessageCreateParams params = paramsBuilder.build();

                if (streamEnabled) {
                    // Streaming 路径：使用修复版解析器，确保 token 数据不被过滤
                    var streamResponse = client.messages().createStreaming(params);
                    return parseStreamEventsWithUsage(
                            Flux.fromStream(streamResponse.stream())
                                    .subscribeOn(Schedulers.boundedElastic()),
                            startTime)
                            .doFinally(signalType -> {
                                try {
                                    streamResponse.close();
                                } catch (Exception e) {
                                    log.debug("Error closing Anthropic stream response", e);
                                }
                            });
                } else {
                    // 非 streaming 路径：parseMessage 原生捕获 inputTokens + outputTokens
                    return Mono.fromFuture(client.async().messages().create(params))
                            .map(message -> AnthropicResponseParser.parseMessage(message, startTime))
                            .flux();
                }
            } catch (Exception e) {
                return Flux.error(new RuntimeException(
                        "Failed to call Anthropic API [model=" + modelName + "]: "
                                + e.getMessage(), e));
            }
        });
    }

    /**
     * 解析 Anthropic streaming 事件 Flux，修复原始 filter 导致 token usage 被丢弃的问题。
     *
     * <p>与框架 {@code AnthropicResponseParser.parseStreamEvents()} 的差异：
     * <ol>
     *   <li>{@code message_start}：缓存 inputTokens，不直接发出（避免后续 {@code message_delta}
     *       的 inputTokens=0 覆盖已记录值）</li>
     *   <li>{@code message_delta}：将缓存的 inputTokens 与 outputTokens 合并发出一个完整
     *       {@link ChatUsage}</li>
     *   <li>filter 放宽为：content 不为空 OR usage 不为 null</li>
     * </ol>
     *
     * @param eventFlux Anthropic SDK 原始流事件
     * @param startTime 请求开始时间，用于计算推理耗时
     * @return 解析后的 {@link ChatResponse} Flux
     */
    private static Flux<ChatResponse> parseStreamEventsWithUsage(
            Flux<RawMessageStreamEvent> eventFlux, Instant startTime) {
        // message_start 先到，将 inputTokens 缓存，message_delta 时合并
        AtomicInteger capturedInputTokens = new AtomicInteger(0);

        return eventFlux
                .flatMap(event -> {
                    try {
                        return parseEvent(event, startTime, capturedInputTokens);
                    } catch (Exception e) {
                        log.warn("Error parsing Anthropic stream event: {}", e.getMessage());
                        return Flux.empty();
                    }
                })
                // 修复：content 不为空 OR 存在 usage 数据均允许通过
                .filter(response -> response != null
                        && (!response.getContent().isEmpty() || response.getUsage() != null));
    }

    /**
     * 解析单个 Anthropic streaming 事件，返回 0 个或 1 个 {@link ChatResponse}。
     *
     * <p>事件处理策略：
     * <ul>
     *   <li>{@code message_start}：缓存 inputTokens，返回空 Flux（不发出事件）</li>
     *   <li>{@code content_block_delta}：提取 text 或 tool input JSON 片段</li>
     *   <li>{@code content_block_start}：提取 tool use 块头部（id + name）</li>
     *   <li>{@code message_delta}：合并 inputTokens + outputTokens，发出完整 usage 事件</li>
     *   <li>其他事件（ping、content_block_stop 等）：发出空响应，被 filter 过滤</li>
     * </ul>
     *
     * @param event               原始流事件
     * @param startTime           请求开始时间
     * @param capturedInputTokens 跨事件共享的 inputTokens 容器
     * @return 解析后的 ChatResponse Flux（0 或 1 个元素）
     */
    private static Flux<ChatResponse> parseEvent(
            RawMessageStreamEvent event,
            Instant startTime,
            AtomicInteger capturedInputTokens) {

        List<ContentBlock> contentBlocks = new ArrayList<>();
        ChatUsage usage = null;
        String messageId = null;

        // message_start：缓存 inputTokens，不发出事件
        // 不在此处发出是因为 ReasoningContext.processChunk() 使用赋值而非累加，
        // 若在此发出后 message_delta 会将 inputTokens 覆盖为 0
        if (event.isMessageStart()) {
            var startEvent = event.asMessageStart();
            int inputTokens = (int) startEvent.message().usage().inputTokens();
            capturedInputTokens.set(inputTokens);
            log.debug("Anthropic message_start: inputTokens={}", inputTokens);
            return Flux.empty();
        }

        // content_block_delta：逐 token 的文本或工具调用 JSON 输入片段
        if (event.isContentBlockDelta()) {
            var deltaEvent = event.asContentBlockDelta();

            // 文本 delta
            deltaEvent.delta().text().ifPresent(
                    textDelta -> contentBlocks.add(
                            TextBlock.builder().text(textDelta.text()).build()));

            // 工具调用 input JSON 片段（片段标记与框架原始实现保持一致）
            deltaEvent.delta().inputJson().ifPresent(
                    jsonDelta -> contentBlocks.add(
                            ToolUseBlock.builder()
                                    .id("")               // 空 ID 表示片段，待后续合并
                                    .name("__fragment__") // 片段标记
                                    .content(jsonDelta.partialJson())
                                    .input(Map.of())
                                    .build()));
        }

        // content_block_start：工具调用块头部，包含 tool name 和 id
        if (event.isContentBlockStart()) {
            event.asContentBlockStart().contentBlock().toolUse().ifPresent(
                    toolUse -> contentBlocks.add(
                            ToolUseBlock.builder()
                                    .id(toolUse.id())
                                    .name(toolUse.name())
                                    .input(Map.of())
                                    .content("")
                                    .build()));
        }

        // message_delta：合并 inputTokens + outputTokens，一次性发出完整 usage
        // 这是 token usage 上报的核心事件
        if (event.isMessageDelta()) {
            var messageDelta = event.asMessageDelta();
            int outputTokens = (int) messageDelta.usage().outputTokens();
            int inputTokens = capturedInputTokens.get();
            usage = ChatUsage.builder()
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .time(Duration.between(startTime, Instant.now()).toMillis() / 1000.0)
                    .build();
            log.debug("Anthropic message_delta: inputTokens={}, outputTokens={}",
                    inputTokens, outputTokens);
        }

        return Flux.just(
                ChatResponse.builder()
                        .id(messageId)
                        .content(contentBlocks)
                        .usage(usage)
                        .build());
    }

    /**
     * 创建 {@link XzagentAnthropicChatModel} 的 Builder 实例。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@link XzagentAnthropicChatModel} 的构建器。
     * 参数与框架 {@code AnthropicChatModel.Builder} 保持一致，方便在 AgentFactory 中直接替换。
     */
    public static class Builder {

        /** Anthropic API 基础 URL，null 时使用 SDK 默认值（https://api.anthropic.com）。 */
        private String baseUrl;

        /** Anthropic API Key，必填。 */
        private String apiKey;

        /** 模型名称，如 claude-opus-4-5，必填。 */
        private String modelName;

        /** 是否启用 streaming 模式，默认 true。 */
        private boolean streamEnabled = true;

        /** 默认生成选项，可选。 */
        private GenerateOptions defaultOptions;

        /** 自定义消息格式化器，null 时使用 {@link AnthropicChatFormatter}。 */
        private AnthropicBaseFormatter formatter;

        /** 设置 Anthropic API 基础 URL（用于代理或私有化部署）。 */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /** 设置 Anthropic API Key。 */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /** 设置模型名称。 */
        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        /** 设置是否启用 streaming 模式。 */
        public Builder stream(boolean streamEnabled) {
            this.streamEnabled = streamEnabled;
            return this;
        }

        /** 设置默认生成选项。 */
        public Builder defaultOptions(GenerateOptions defaultOptions) {
            this.defaultOptions = defaultOptions;
            return this;
        }

        /** 设置自定义消息格式化器（可选，默认使用 {@link AnthropicChatFormatter}）。 */
        public Builder formatter(AnthropicBaseFormatter formatter) {
            this.formatter = formatter;
            return this;
        }

        /**
         * 构建 {@link XzagentAnthropicChatModel} 实例。
         *
         * @return 配置好的模型实例
         */
        public XzagentAnthropicChatModel build() {
            // 构建 Anthropic SDK 客户端，与框架 AnthropicChatModel 构造方式完全一致
            AnthropicOkHttpClient.Builder clientBuilder = AnthropicOkHttpClient.builder();
            if (apiKey != null) {
                clientBuilder.apiKey(apiKey);
            }
            if (baseUrl != null && !baseUrl.isBlank()) {
                clientBuilder.baseUrl(baseUrl);
            }
            AnthropicClient client = clientBuilder.build();

            AnthropicBaseFormatter resolvedFormatter =
                    formatter != null ? formatter : new AnthropicChatFormatter();
            GenerateOptions resolvedOptions =
                    defaultOptions != null ? defaultOptions : GenerateOptions.builder().build();

            return new XzagentAnthropicChatModel(
                    modelName, streamEnabled, client, resolvedFormatter, resolvedOptions);
        }
    }
}
