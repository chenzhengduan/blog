package xiaogj.xzagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.plan.PlanNotebook;
import xiaogj.xzagent.model.XzagentAnthropicChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolExecutionContext;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.ToolkitConfig;
import xiaogj.xzagent.tool.AgentRunToolContext;
import xiaogj.xzagent.tool.UserToolContext;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import xiaogj.xzagent.config.XzagentAnthropicProperties;
import xiaogj.xzagent.config.XzagentAutoContextProperties;
import xiaogj.xzagent.config.XzagentModelProperties;
import xiaogj.xzagent.config.XzagentOpenAiProperties;
import xiaogj.xzagent.infrastructure.session.MysqlAgentSession;
import xiaogj.xzagent.repository.A2uiSurfaceRepository;
import xiaogj.xzagent.repository.AgentRunRepository;
import xiaogj.xzagent.repository.ToolAuditRepository;
import xiaogj.xzagent.repository.TokenUsageRepository;
import xiaogj.xzagent.service.hook.A2uiSurfaceTrackingHook;
import xiaogj.xzagent.service.hook.XzagentAutoContextHook;
import xiaogj.xzagent.service.hook.ToolAuditHook;
import xiaogj.xzagent.service.hook.TokenUsageHook;
import xiaogj.xzagent.tool.RenderA2uiTool;
import xiaogj.xzagent.util.SessionIdValidator;
import org.springframework.stereotype.Component;

@Component
public class AgentFactory {

    /** Anthropic Claude 系列模型接入配置。 */
    private final XzagentAnthropicProperties anthropicProperties;
    /** OpenAI 及兼容服务接入配置。 */
    private final XzagentOpenAiProperties openAiProperties;
    /** 模型供应商总开关配置，决定运行时使用哪个服务商。 */
    private final XzagentModelProperties modelProperties;
    private final XzagentAutoContextProperties autoContextProperties;
    private final RuntimeSnapshotRegistry runtimeSnapshotRegistry;
    private final SkillBindingService skillBindingService;
    private final SessionActiveSkillService sessionActiveSkillService;
    private final MysqlAgentSession session;
    private final AgentRunRepository agentRunRepository;
    private final ToolAuditRepository toolAuditRepository;
    private final List<AgentTool> builtInAgentTools;
    private final RenderA2uiTool renderA2uiTool;
    private final ObjectMapper objectMapper;
    private final A2uiSurfaceRepository a2uiSurfaceRepository;
    private final TokenUsageRepository tokenUsageRepository;
    private final NodeIdentityProvider nodeIdentityProvider;
    private final MeterRegistry meterRegistry;

    public AgentFactory(
            XzagentAnthropicProperties anthropicProperties,
            XzagentOpenAiProperties openAiProperties,
            XzagentModelProperties modelProperties,
            XzagentAutoContextProperties autoContextProperties,
            RuntimeSnapshotRegistry runtimeSnapshotRegistry,
            SkillBindingService skillBindingService,
            SessionActiveSkillService sessionActiveSkillService,
            MysqlAgentSession session,
            AgentRunRepository agentRunRepository,
            ToolAuditRepository toolAuditRepository,
            List<AgentTool> builtInAgentTools,
            RenderA2uiTool renderA2uiTool,
            ObjectMapper objectMapper,
            A2uiSurfaceRepository a2uiSurfaceRepository,
            TokenUsageRepository tokenUsageRepository,
            NodeIdentityProvider nodeIdentityProvider,
            MeterRegistry meterRegistry) {
        this.anthropicProperties = anthropicProperties;
        this.openAiProperties = openAiProperties;
        this.modelProperties = modelProperties;
        this.autoContextProperties = autoContextProperties;
        this.runtimeSnapshotRegistry = runtimeSnapshotRegistry;
        this.skillBindingService = skillBindingService;
        this.sessionActiveSkillService = sessionActiveSkillService;
        this.session = session;
        this.agentRunRepository = agentRunRepository;
        this.toolAuditRepository = toolAuditRepository;
        this.builtInAgentTools = builtInAgentTools;
        this.renderA2uiTool = renderA2uiTool;
        this.objectMapper = objectMapper;
        this.a2uiSurfaceRepository = a2uiSurfaceRepository;
        this.tokenUsageRepository = tokenUsageRepository;
        this.nodeIdentityProvider = nodeIdentityProvider;
        this.meterRegistry = meterRegistry;
    }

    public AgentRuntime create(String rawSessionId, String runId, Long userId, String username) {
        long startedAt = System.nanoTime();
        String sessionId = SessionIdValidator.validate(rawSessionId);
        RuntimeSnapshot snapshot = runtimeSnapshotRegistry.getSnapshot();
        var agentDefinition = snapshot.agentDefinition();
        // 将用户身份注入 Toolkit 上下文，供工具在服务端取用（AI 不可见）
        ToolExecutionContext userContext = ToolExecutionContext.builder()
                .register(new UserToolContext(userId, username))
                .register(new AgentRunToolContext(sessionId, runId))
                .build();
        Toolkit toolkit = new Toolkit(ToolkitConfig.builder().defaultContext(userContext).build());
        toolkit.registration().agentTool(renderA2uiTool).apply();
        SkillBox skillBox = new SkillBox(toolkit);
        Set<String> selectedSkillIds = agentDefinition.skillIds().stream().collect(Collectors.toSet());
        for (AgentSkill skill : snapshot.skills()) {
            if (!selectedSkillIds.contains(skill.getName())) {
                continue;
            }
            skillBox.registration().toolkit(toolkit).skill(skill).apply();
        }
        skillBox.registerSkillLoadTool();
        skillBindingService.applyBindings(snapshot, skillBox, toolkit, userId, username);

        // 根据供应商配置动态选择模型实现，支持 Anthropic / OpenAI 及兼容服务
        String modelProvider = modelProperties.provider().name().toLowerCase();
        String modelName = switch (modelProperties.provider()) {
            case ANTHROPIC -> anthropicProperties.modelName();
            case OPENAI -> openAiProperties.modelName();
        };
        Model model = createModel();

        AutoContextConfig autoContextConfig = AutoContextConfig.builder()
                .largePayloadThreshold(autoContextProperties.largePayloadThreshold())
                .maxToken(autoContextProperties.maxToken())
                .tokenRatio(autoContextProperties.tokenRatio())
                .offloadSinglePreview(autoContextProperties.offloadSinglePreview())
                .msgThreshold(autoContextProperties.msgThreshold())
                .lastKeep(autoContextProperties.lastKeep())
                .minConsecutiveToolMessages(autoContextProperties.minConsecutiveToolMessages())
                .currentRoundCompressionRatio(autoContextProperties.currentRoundCompressionRatio())
                .minCompressionTokenThreshold(autoContextProperties.minCompressionTokenThreshold())
                .build();
        AutoContextMemory memory = new AutoContextMemory(autoContextConfig, model);
        PlanNotebook planNotebook = PlanNotebook.builder()
                .needUserConfirm(false)
                .build();

        SimpleSessionKey sessionKey = SimpleSessionKey.of(sessionId);
        A2uiSurfaceTrackingHook surfaceTrackingHook =
                new A2uiSurfaceTrackingHook(session, sessionKey, objectMapper, a2uiSurfaceRepository);

        ReActAgent agent = ReActAgent.builder()
                .name(agentDefinition.name())
                .sysPrompt(agentDefinition.effectiveSystemPrompt())
                .maxIters(agentDefinition.maxIterations())
                .model(model)
                .memory(memory)
                .planNotebook(planNotebook)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .hooks(List.of(
                        new XzagentAutoContextHook(),
                        surfaceTrackingHook,
                        new ToolAuditHook(
                                runId,
                                toolAuditRepository,
                                builtInAgentTools.stream()
                                        .map(AgentTool::getName)
                                        .collect(Collectors.toSet())),
                        new TokenUsageHook(
                                runId,
                                userId,
                                sessionId,
                                agentDefinition.agentId(),
                                modelProvider,
                                modelName,
                                tokenUsageRepository)))
                .build();

        session.recordUser(sessionId, userId);
        agent.loadIfExists(session, sessionKey);
        sessionActiveSkillService.restore(sessionId, toolkit);
        agentRunRepository.createRunning(runId, sessionId, nodeIdentityProvider.currentNodeId());
        AgentRuntime runtime = new AgentRuntime(
                sessionId,
                runId,
                userId,
                username,
                agent,
                skillBox,
                session,
                sessionKey,
                surfaceTrackingHook);
        meterRegistry.counter("xzagent.agent.runtime.create", "result", "executed").increment();
        meterRegistry.timer("xzagent.agent.runtime.create.duration")
                .record(System.nanoTime() - startedAt, TimeUnit.NANOSECONDS);
        return runtime;
    }

    /**
     * 根据配置构建当前 Agent 使用的模型实例。
     *
     * <p>AgentScope 的 {@link Model} 是 ReActAgent 和 AutoContextMemory 的统一抽象，
     * 因此这里把不同服务商的 Builder 差异收敛在工厂内部，避免业务运行时感知具体模型实现。
     */
    private Model createModel() {
        return switch (modelProperties.provider()) {
            case ANTHROPIC -> createAnthropicModel();
            case OPENAI -> createOpenAiModel();
        };
    }

    /**
     * 构建 Anthropic Claude 模型。
     *
     * <p>这是 Xzagent 历史默认模型路径，保留原有环境变量和默认模型名不变。
     */
    private XzagentAnthropicChatModel createAnthropicModel() {
        requireConfigured(anthropicProperties.apiKey(), "ANTHROPIC_API_KEY");
        requireConfigured(anthropicProperties.modelName(), "XZAGENT_ANTHROPIC_MODEL");
        // 使用自定义实现，修复 streaming 模式下 token usage 上报问题
        XzagentAnthropicChatModel.Builder modelBuilder = XzagentAnthropicChatModel.builder()
                .apiKey(anthropicProperties.apiKey())
                .modelName(anthropicProperties.modelName())
                .stream(anthropicProperties.streamEnabled())
                .defaultOptions(GenerateOptions.builder().build());
        if (hasText(anthropicProperties.baseUrl())) {
            modelBuilder.baseUrl(anthropicProperties.baseUrl());
        }
        return modelBuilder.build();
    }

    /**
     * 构建 OpenAI 或 OpenAI API 兼容模型。
     *
     * <p>AgentScope 的 {@link OpenAIChatModel} 兼容 OpenAI API 规范，
     * 因此配置 {@code baseUrl} 后也可接入 DeepSeek、vLLM 等兼容服务。
     */
    private OpenAIChatModel createOpenAiModel() {
        requireConfigured(openAiProperties.apiKey(), "OPENAI_API_KEY");
        requireConfigured(openAiProperties.modelName(), "XZAGENT_OPENAI_MODEL");
        OpenAIChatModel.Builder modelBuilder = OpenAIChatModel.builder()
                .apiKey(openAiProperties.apiKey())
                .modelName(openAiProperties.modelName())
                .stream(openAiProperties.streamEnabled())
                .generateOptions(GenerateOptions.builder().build());
        if (hasText(openAiProperties.baseUrl())) {
            modelBuilder.baseUrl(openAiProperties.baseUrl());
        }
        return modelBuilder.build();
    }

    /**
     * 校验被选中模型服务商的关键配置，避免运行期请求模型时才暴露空配置问题。
     *
     * @param value 配置值
     * @param name  对应的环境变量名，用于错误提示
     */
    private void requireConfigured(String value, String name) {
        if (!hasText(value)) {
            throw new IllegalStateException(name + " 不能为空，请检查当前模型服务商配置");
        }
    }

    /**
     * 判断字符串是否包含非空白字符。
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
