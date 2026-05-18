package xiaogj.xzagent.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.tool.AgentTool;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import xiaogj.xzagent.config.XzagentAnthropicProperties;
import xiaogj.xzagent.config.XzagentAutoContextProperties;
import xiaogj.xzagent.config.XzagentModelProperties;
import xiaogj.xzagent.config.XzagentModelProvider;
import xiaogj.xzagent.config.XzagentOpenAiProperties;
import xiaogj.xzagent.infrastructure.session.MysqlAgentSession;
import xiaogj.xzagent.model.AgentDefinition;
import xiaogj.xzagent.repository.A2uiSurfaceRepository;
import xiaogj.xzagent.repository.AgentRunRepository;
import xiaogj.xzagent.repository.TokenUsageRepository;
import xiaogj.xzagent.repository.ToolAuditRepository;
import xiaogj.xzagent.tool.RenderA2uiTool;

/**
 * {@link AgentFactory} 单元测试。
 */
class AgentFactoryTest {

    /**
     * 创建运行时时，应为 ReActAgent 挂载 PlanNotebook，且默认无需用户确认。
     */
    @Test
    void createShouldAttachPlanNotebookWithoutUserConfirmation() {
        RuntimeSnapshotRegistry runtimeSnapshotRegistry = mock(RuntimeSnapshotRegistry.class);
        SkillBindingService skillBindingService = mock(SkillBindingService.class);
        SessionActiveSkillService sessionActiveSkillService = mock(SessionActiveSkillService.class);
        MysqlAgentSession session = mock(MysqlAgentSession.class);
        AgentRunRepository agentRunRepository = mock(AgentRunRepository.class);
        ToolAuditRepository toolAuditRepository = mock(ToolAuditRepository.class);
        A2uiSurfaceRepository a2uiSurfaceRepository = mock(A2uiSurfaceRepository.class);
        TokenUsageRepository tokenUsageRepository = mock(TokenUsageRepository.class);
        NodeIdentityProvider nodeIdentityProvider = mock(NodeIdentityProvider.class);

        when(runtimeSnapshotRegistry.getSnapshot()).thenReturn(new RuntimeSnapshot(
                new AgentDefinition(
                        "default",
                        "test-agent",
                        "system prompt",
                        null,
                        8,
                        List.of(),
                        true,
                        Instant.parse("2026-05-04T00:00:00Z"),
                        Instant.parse("2026-05-04T00:00:00Z")),
                List.of(),
                Map.of(),
                List.of(),
                List.of(),
                List.of(),
                Map.of(),
                Map.of()));
        doNothing().when(skillBindingService).applyBindings(any(), any(), any(), eq(1L), eq("tester"));
        when(nodeIdentityProvider.currentNodeId()).thenReturn("xzagent:127.0.0.1:8080");
        when(session.exists(any())).thenReturn(false);

        AgentFactory agentFactory = new AgentFactory(
                new XzagentAnthropicProperties("unused-key", "claude-sonnet", true, null),
                new XzagentOpenAiProperties("test-key", "gpt-4o-mini", true, null, null),
                new XzagentModelProperties(XzagentModelProvider.OPENAI),
                new XzagentAutoContextProperties(2048, 100000, 0.75, 120, 30, 16, 3, 0.35, 1200),
                runtimeSnapshotRegistry,
                skillBindingService,
                sessionActiveSkillService,
                session,
                agentRunRepository,
                toolAuditRepository,
                List.<AgentTool>of(),
                new RenderA2uiTool(new ObjectMapper()),
                new ObjectMapper(),
                a2uiSurfaceRepository,
                tokenUsageRepository,
                nodeIdentityProvider,
                new SimpleMeterRegistry());

        AgentRuntime runtime = agentFactory.create("session-1", "run-1", 1L, "tester");

        PlanNotebook planNotebook = runtime.agent().getPlanNotebook();
        assertNotNull(planNotebook);
        assertFalse(planNotebook.isNeedUserConfirm());
        assertNotNull(runtime.agent().getToolkit().getTool("create_plan"));
        verify(session).recordUser("session-1", 1L);
        verify(sessionActiveSkillService).restore(eq("session-1"), any());
        verify(agentRunRepository).createRunning("run-1", "session-1", "xzagent:127.0.0.1:8080");
    }
}
