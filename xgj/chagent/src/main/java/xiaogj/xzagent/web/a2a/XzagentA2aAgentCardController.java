package xiaogj.xzagent.web.a2a;

import io.a2a.spec.AgentCard;
import io.agentscope.core.a2a.server.AgentScopeA2aServer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Xzagent A2A AgentCard 暴露接口。
 *
 * <p>A2A 客户端通常先读取 well-known agent card，再决定是否以及如何调用该 Agent。
 * 因此该接口保持公开，只返回 A2A Server 当前构建出的 AgentCard 元数据。
 */
@RestController
@RequestMapping("/xzagent")
public class XzagentA2aAgentCardController {

    private final AgentScopeA2aServer agentScopeA2aServer;

    public XzagentA2aAgentCardController(AgentScopeA2aServer agentScopeA2aServer) {
        this.agentScopeA2aServer = agentScopeA2aServer;
    }

    /**
     * 返回默认 agent 的 AgentCard。
     */
    @GetMapping(value = "/.well-known/agent-card.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public AgentCard getAgentCard() {
        return agentScopeA2aServer.getAgentCard();
    }
}
