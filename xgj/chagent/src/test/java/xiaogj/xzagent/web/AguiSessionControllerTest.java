package xiaogj.xzagent.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.agentscope.core.agui.encoder.AguiEventEncoder;
import io.agentscope.core.agui.event.AguiEvent;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import xiaogj.xzagent.config.XzagentDistributedRunProperties;
import xiaogj.xzagent.model.AgentRunStatus;
import xiaogj.xzagent.model.DistributedRunMetadata;
import xiaogj.xzagent.model.UserRole;
import xiaogj.xzagent.service.AgentRunFinalizationService;
import xiaogj.xzagent.service.DistributedRunCoordinator;
import xiaogj.xzagent.service.NodeIdentityProvider;
import xiaogj.xzagent.service.RunEventStreamStore;
import xiaogj.xzagent.service.RunHeartbeatService;
import xiaogj.xzagent.service.RunStreamRegistry;
import xiaogj.xzagent.service.UserPrincipal;

/**
 * {@link AguiSessionController} 单元测试。
 */
class AguiSessionControllerTest {

    private static final AguiEventEncoder AGUI_EVENT_ENCODER = new AguiEventEncoder();

    /**
     * 当会话没有活跃运行时，应立即返回 noActiveRun 信号事件。
     */
    @Test
    void reconnectStreamShouldReturnNoActiveRunSignalWhenSessionHasNoActiveRun() {
        DistributedRunCoordinator distributedRunCoordinator = mock(DistributedRunCoordinator.class);
        when(distributedRunCoordinator.findActiveRun("session-1")).thenReturn(Optional.empty());
        AguiSessionController controller = createController(distributedRunCoordinator, mock(RunEventStreamStore.class));

        Flux<ServerSentEvent<String>> flux = controller.reconnectStream(userPrincipal(), "session-1", null);

        StepVerifier.create(flux)
                .assertNext(event -> {
                    assertTrue(event.data().contains("\"noActiveRun\":true"));
                    assertEquals(null, event.id());
                })
                .verifyComplete();
    }

    /**
     * 当重连请求携带 Last-Event-ID 时，应只补发缺失事件，并在收到 RunFinished 后结束流。
     */
    @Test
    void reconnectStreamShouldReplayMissingEventsAfterLastEventIdAndStopAtRunFinished() {
        DistributedRunCoordinator distributedRunCoordinator = mock(DistributedRunCoordinator.class);
        RunEventStreamStore runEventStreamStore = mock(RunEventStreamStore.class);
        DistributedRunMetadata metadata = new DistributedRunMetadata(
                "session-1",
                "run-1",
                "node-a",
                "http://node-a:8080",
                AgentRunStatus.RUNNING,
                3L,
                java.time.Instant.parse("2026-05-03T00:00:00Z"),
                java.time.Instant.parse("2026-05-03T00:00:01Z"),
                false);
        when(distributedRunCoordinator.findActiveRun("session-1")).thenReturn(Optional.of(metadata));
        when(runEventStreamStore.replayAfter(eq("run-1"), eq(1L), anyInt())).thenReturn(List.of(
                storedEvent(2L, new AguiEvent.TextMessageContent("session-1", "run-1", "msg-1", "hello")),
                storedEvent(3L, new AguiEvent.RunFinished("session-1", "run-1"))));

        AguiSessionController controller = createController(distributedRunCoordinator, runEventStreamStore);
        Flux<ServerSentEvent<String>> flux = controller.reconnectStream(userPrincipal(), "session-1", "1");

        StepVerifier.create(flux)
                .assertNext(event -> {
                    assertEquals("2", event.id());
                    assertTrue(event.data().contains("\"type\":\"TEXT_MESSAGE_CONTENT\""));
                })
                .assertNext(event -> {
                    assertEquals("3", event.id());
                    assertTrue(event.data().contains("\"type\":\"RUN_FINISHED\""));
                })
                .verifyComplete();
        verify(runEventStreamStore).replayAfter("run-1", 1L, 100);
        verify(distributedRunCoordinator, never()).findRun("run-1");
    }

    /**
     * 当补流阶段没有终止事件，且随后运行元数据消失时，应结束追尾流而不是一直轮询。
     */
    @Test
    void reconnectStreamShouldCompleteWhenTailFindsNoMoreMetadata() {
        DistributedRunCoordinator distributedRunCoordinator = mock(DistributedRunCoordinator.class);
        RunEventStreamStore runEventStreamStore = mock(RunEventStreamStore.class);
        DistributedRunMetadata metadata = new DistributedRunMetadata(
                "session-1",
                "run-1",
                "node-a",
                "http://node-a:8080",
                AgentRunStatus.RUNNING,
                1L,
                java.time.Instant.parse("2026-05-03T00:00:00Z"),
                java.time.Instant.parse("2026-05-03T00:00:01Z"),
                false);
        when(distributedRunCoordinator.findActiveRun("session-1")).thenReturn(Optional.of(metadata));
        when(runEventStreamStore.replayAfter("run-1", 0L, 100)).thenReturn(List.of(
                storedEvent(1L, new AguiEvent.TextMessageContent("session-1", "run-1", "msg-1", "hello"))));
        when(runEventStreamStore.replayAfter("run-1", 1L, 100)).thenReturn(List.of());
        when(distributedRunCoordinator.findRun("run-1")).thenReturn(Optional.empty());

        AguiSessionController controller = createController(
                distributedRunCoordinator,
                runEventStreamStore,
                new XzagentDistributedRunProperties("node-a", "http://node-a:8080", 120, 10, 3600, 100, 10, "/xzagent/internal"));

        StepVerifier.create(controller.reconnectStream(userPrincipal(), "session-1", "0"))
                .assertNext(event -> {
                    assertEquals("1", event.id());
                    assertTrue(event.data().contains("\"type\":\"TEXT_MESSAGE_CONTENT\""));
                })
                .expectComplete()
                .verify(Duration.ofSeconds(1));
        verify(runEventStreamStore).replayAfter("run-1", 0L, 100);
        verify(runEventStreamStore).replayAfter("run-1", 1L, 100);
        verify(distributedRunCoordinator).findRun("run-1");
    }

    private AguiSessionController createController(
            DistributedRunCoordinator distributedRunCoordinator,
            RunEventStreamStore runEventStreamStore) {
        return createController(
                distributedRunCoordinator,
                runEventStreamStore,
                new XzagentDistributedRunProperties(
                        "node-a",
                        "http://node-a:8080",
                        120,
                        10,
                        3600,
                        100,
                        50,
                        "/xzagent/internal"));
    }

    private AguiSessionController createController(
            DistributedRunCoordinator distributedRunCoordinator,
            RunEventStreamStore runEventStreamStore,
            XzagentDistributedRunProperties distributedRunProperties) {
        RunStreamRegistry runStreamRegistry = mock(RunStreamRegistry.class);
        RunHeartbeatService runHeartbeatService = mock(RunHeartbeatService.class);
        NodeIdentityProvider nodeIdentityProvider = mock(NodeIdentityProvider.class);
        when(nodeIdentityProvider.currentNodeId()).thenReturn("node-a");
        when(nodeIdentityProvider.internalControlPathPrefix()).thenReturn("/xzagent/internal");
        AgentRunFinalizationService agentRunFinalizationService = mock(AgentRunFinalizationService.class);
        return new AguiSessionController(
                runStreamRegistry,
                distributedRunCoordinator,
                runEventStreamStore,
                runHeartbeatService,
                nodeIdentityProvider,
                distributedRunProperties,
                WebClient.builder(),
                agentRunFinalizationService);
    }

    private UserPrincipal userPrincipal() {
        return new UserPrincipal(1L, "tester", UserRole.USER, true);
    }

    private RunEventStreamStore.StoredRunEvent storedEvent(long seq, AguiEvent event) {
        return new RunEventStreamStore.StoredRunEvent("record-" + seq, seq, AGUI_EVENT_ENCODER.encodeToJson(event).trim());
    }
}
