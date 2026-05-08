package xiaogj.xzagent.service;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.state.SimpleSessionKey;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.infrastructure.session.MysqlAgentSession;
import xiaogj.xzagent.util.SessionIdValidator;
import org.springframework.stereotype.Service;

/**
 * 处理 A2UI client-to-server message 的应用服务。
 */
@Service
public class A2uiMessageApplicationService {

    /**
     * AGUI 输入消息模型不会透传 metadata，因此需要保留一个服务端专用 ID 前缀，
     * 让这些 A2UI action 代理消息在后续落入 originalMessages 后仍可被识别并从历史中隐藏。
     */
    public static final String A2UI_ACTION_MESSAGE_ID_PREFIX = "a2ui-action-";
    private static final String SESSION_KEY_A2UI_CLIENT_MESSAGES = "a2ui_client_messages";

    private final MysqlAgentSession session;
    private final MeterRegistry meterRegistry;

    public A2uiMessageApplicationService(MysqlAgentSession session, MeterRegistry meterRegistry) {
        this.session = session;
        this.meterRegistry = meterRegistry;
    }

    public void acceptClientMessage(String rawSessionId, Map<String, Object> payload) {
        String sessionId = SessionIdValidator.validate(rawSessionId);
        validatePayload(payload);
        SimpleSessionKey sessionKey = SimpleSessionKey.of(sessionId);
        Msg message = Msg.builder()
                .name("a2ui-client")
                .role(MsgRole.USER)
                .textContent(writeSummary(payload))
                .metadata(Map.of(
                        "kind", "a2ui_action",
                        "hiddenInHistory", true,
                        "a2ui", payload))
                .build();
        List<Msg> history = new ArrayList<>(
                session.getList(sessionKey, SESSION_KEY_A2UI_CLIENT_MESSAGES, Msg.class));
        history.add(message);
        session.save(sessionKey, SESSION_KEY_A2UI_CLIENT_MESSAGES, history);
    }

    public List<Msg> listClientMessages(String rawSessionId) {
        String sessionId = SessionIdValidator.validate(rawSessionId);
        SimpleSessionKey sessionKey = SimpleSessionKey.of(sessionId);
        return new ArrayList<>(
                session.getList(sessionKey, SESSION_KEY_A2UI_CLIENT_MESSAGES, Msg.class));
    }

    public void clearClientMessages(String rawSessionId) {
        String sessionId = SessionIdValidator.validate(rawSessionId);
        session.delete(SimpleSessionKey.of(sessionId), SESSION_KEY_A2UI_CLIENT_MESSAGES);
        meterRegistry.counter("xzagent.agui.pending-client-messages.clear", "result", "executed")
                .increment();
    }

    public void recordClearClientMessagesSkipped() {
        meterRegistry.counter("xzagent.agui.pending-client-messages.clear", "result", "skipped")
                .increment();
    }

    private void validatePayload(Map<String, Object> payload) {
        Object version = payload.get("version");
        if (!(version instanceof String versionText) || versionText.isBlank()) {
            throw new IllegalArgumentException("A2UI message 缺少 version");
        }
        boolean hasAction = payload.get("action") instanceof Map<?, ?>;
        boolean hasError = payload.get("error") instanceof Map<?, ?>;
        if (hasAction == hasError) {
            throw new IllegalArgumentException("A2UI client message 必须且只能包含 action 或 error");
        }
    }

    private String writeSummary(Map<String, Object> payload) {
        if (payload.get("action") instanceof Map<?, ?> action) {
            Object name = action.get("name");
            Object surfaceId = action.get("surfaceId");
            Object context = action.get("context");
            return "A2UI action: name=" + String.valueOf(name)
                    + ", surfaceId=" + String.valueOf(surfaceId)
                    + ", context=" + String.valueOf(context);
        }
        if (payload.get("error") instanceof Map<?, ?> error) {
            Object message = error.get("message");
            return "A2UI error: message=" + String.valueOf(message);
        }
        return "A2UI client message";
    }
}
