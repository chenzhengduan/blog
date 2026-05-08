package xiaogj.xzagent.service;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SimpleSessionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import xiaogj.xzagent.repository.A2uiSurfaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class A2uiSurfaceMessageBindingService {

    private static final Logger log = LoggerFactory.getLogger(A2uiSurfaceMessageBindingService.class);
    private static final String MESSAGE_SESSION_KEY = "autoContextMemory_originalMessages";

    private final A2uiSurfaceRepository a2uiSurfaceRepository;

    public A2uiSurfaceMessageBindingService(A2uiSurfaceRepository a2uiSurfaceRepository) {
        this.a2uiSurfaceRepository = a2uiSurfaceRepository;
    }

    public void bindDirtySurfacesToLastAssistantMessage(
            Session session,
            SimpleSessionKey sessionKey,
            Set<String> dirtySurfaceIds) {
        if (dirtySurfaceIds == null || dirtySurfaceIds.isEmpty()) {
            return;
        }
        List<Msg> messages = new ArrayList<>(session.getList(sessionKey, MESSAGE_SESSION_KEY, Msg.class));
        Msg lastAssistant = findLastAssistant(messages);
        if (lastAssistant == null) {
            log.warn("未找到可绑定的 assistant 消息: sessionId={}", sessionKey.toIdentifier());
            return;
        }
        Msg resolvedAssistant = ensureMessageId(session, sessionKey, messages, lastAssistant);
        a2uiSurfaceRepository.updateLastUpdatedMsgId(
                sessionKey.toIdentifier(),
                dirtySurfaceIds,
                resolvedAssistant.getId());
    }

    private Msg ensureMessageId(Session session, SimpleSessionKey sessionKey, List<Msg> messages, Msg message) {
        if (message.getId() != null && !message.getId().isBlank()) {
            return message;
        }
        Msg updatedMessage = Msg.builder()
                .id("assistant-" + UUID.randomUUID())
                .name(message.getName())
                .role(message.getRole())
                .content(message.getContent())
                .metadata(message.getMetadata())
                .timestamp(message.getTimestamp())
                .build();
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i) == message) {
                messages.set(i, updatedMessage);
                session.save(sessionKey, MESSAGE_SESSION_KEY, messages);
                return updatedMessage;
            }
        }
        return updatedMessage;
    }

    private Msg findLastAssistant(List<Msg> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            Msg candidate = messages.get(i);
            if (candidate.getRole() == MsgRole.ASSISTANT) {
                return candidate;
            }
        }
        return null;
    }
}
