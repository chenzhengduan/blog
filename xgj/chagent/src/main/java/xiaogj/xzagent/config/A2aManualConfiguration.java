package xiaogj.xzagent.config;

import io.a2a.spec.APIKeySecurityScheme;
import io.agentscope.core.a2a.server.AgentScopeA2aServer;
import io.agentscope.core.a2a.server.card.ConfigurableAgentCard;
import io.agentscope.core.a2a.server.executor.AgentExecuteProperties;
import io.agentscope.core.a2a.server.transport.DeploymentProperties;
import io.agentscope.core.a2a.server.transport.TransportProperties;
import io.a2a.spec.TransportProtocol;
import xiaogj.xzagent.service.a2a.XzagentDefaultAgentRunner;
import xiaogj.xzagent.web.a2a.XzagentA2aAgentCardController;
import xiaogj.xzagent.web.a2a.XzagentA2aJsonRpcController;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 手动 A2A 装配配置。
 *
 * <p>当前不使用 AgentScope 的 A2A Spring Boot starter 自动装配，而是显式创建
 * A2A Server 和对应 Controller。这样可以保持对默认 agent 暴露策略的完全控制，
 * 并避免后续扩展多 agent 时被 starter 的单 server 约束。
 */
@Configuration
@ConditionalOnProperty(prefix = "xzagent.a2a", name = "enabled", havingValue = "true")
public class A2aManualConfiguration {

    private static final String A2A_API_KEY_SCHEME = "xzagentApiKey";

    /**
     * 创建默认 agent 的 A2A Server。
     *
     * <p>这里只暴露 JSON-RPC transport。Web 层由自定义 Controller 把 HTTP 请求交给
     * A2A transport wrapper 处理。
     */
    @Bean
    public AgentScopeA2aServer xzagentA2aServer(
            XzagentA2aProperties properties,
            XzagentDefaultAgentRunner agentRunner) {
        ConfigurableAgentCard agentCard = new ConfigurableAgentCard.Builder()
                .name(properties.cardName())
                .description(properties.cardDescription())
                .version(properties.version())
                .url(buildAgentCardUrl(properties))
                .securitySchemes(Map.of(
                        A2A_API_KEY_SCHEME,
                        new APIKeySecurityScheme(
                                "使用 X-API-Key 传递 xzagent 用户 API Key。",
                                "header",
                                A2aApiKeyAuthenticationConverter.API_KEY_HEADER)))
                .security(List.of(Map.of(A2A_API_KEY_SCHEME, List.of())))
                .build();

        return AgentScopeA2aServer.builder(agentRunner)
                .agentCard(agentCard)
                .withTransport(TransportProperties.builder(TransportProtocol.JSONRPC.asString())
                        .host(properties.host())
                        .port(properties.port())
                        .path("/xzagent")
                        .build())
                .deploymentProperties(new DeploymentProperties.Builder()
                        .host(properties.host())
                        .port(properties.port())
                        .build())
                .agentExecuteProperties(AgentExecuteProperties.builder()
                        .completeWithMessage(true)
                        .requireInnerMessage(false)
                        .build())
                .build();
    }

    /**
     * 对外暴露 AgentCard。
     */
    @Bean
    public XzagentA2aAgentCardController xzagentA2aAgentCardController(
            AgentScopeA2aServer xzagentA2aServer) {
        return new XzagentA2aAgentCardController(xzagentA2aServer);
    }

    /**
     * 对外暴露 JSON-RPC 请求入口。
     */
    @Bean
    public XzagentA2aJsonRpcController xzagentA2aJsonRpcController(
            AgentScopeA2aServer xzagentA2aServer,
            ObjectMapper objectMapper) {
        return new XzagentA2aJsonRpcController(xzagentA2aServer, objectMapper);
    }

    /**
     * 在 Spring Web 端点就绪后执行 A2A Server 的注册回调。
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> xzagentA2aServerReadyListener(
            AgentScopeA2aServer xzagentA2aServer) {
        return event -> xzagentA2aServer.postEndpointReady();
    }

    private String buildAgentCardUrl(XzagentA2aProperties properties) {
        return "http://" + properties.host() + ":" + properties.port() + "/xzagent";
    }
}
