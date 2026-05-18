package xiaogj.xzagent.web.a2a;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.a2a.spec.JSONRPCResponse;
import io.a2a.spec.TransportProtocol;
import io.a2a.util.Utils;
import io.agentscope.core.a2a.server.AgentScopeA2aServer;
import io.agentscope.core.a2a.server.transport.jsonrpc.JsonRpcTransportWrapper;
import java.util.Map;
import xiaogj.xzagent.service.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Xzagent A2A JSON-RPC 入口。
 *
 * <p>当前按 AgentScope 官方推荐方式，将 HTTP POST `/xzagent` 请求转交给
 * {@link JsonRpcTransportWrapper} 处理。这样可以在保持 Spring Boot 应用形态的前提下，
 * 暴露一个标准 A2A JSON-RPC 接口。
 */
@RestController
@RequestMapping("/xzagent")
public class XzagentA2aJsonRpcController {

    private static final Logger log =
            LoggerFactory.getLogger(XzagentA2aJsonRpcController.class);

    private final AgentScopeA2aServer agentScopeA2aServer;
    private final ObjectMapper objectMapper;

    private JsonRpcTransportWrapper jsonRpcTransportWrapper;

    public XzagentA2aJsonRpcController(
            AgentScopeA2aServer agentScopeA2aServer,
            ObjectMapper objectMapper) {
        this.agentScopeA2aServer = agentScopeA2aServer;
        this.objectMapper = objectMapper;
    }

    /**
     * 接收 JSON-RPC 请求并转交给 A2A transport 处理。
     *
     * <p>当 A2A Server 以流式方式返回 JSON-RPC 响应时，这里会把每个响应包装成
     * SSE 事件，保持与官方 starter 的兼容行为。
     */
    @PostMapping(
            value = "",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    @ResponseBody
    public Object handleRequest(
            @RequestBody String body,
            @RequestHeader Map<String, String> headers,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info(
                "收到 A2A JSON-RPC 请求: contentLength={}, username={}, headerKeys={}",
                body != null ? body.length() : 0,
                principal != null ? principal.username() : "anonymous",
                headers.keySet());
        String normalizedBody = injectAuthenticatedUser(body, principal);
        Object result = getJsonRpcTransportWrapper().handleRequest(
                normalizedBody,
                headers,
                Map.of());
        if (!(result instanceof Flux<?> fluxResult)) {
            return result;
        }
        return fluxResult
                .filter(each -> each instanceof JSONRPCResponse)
                .map(each -> (JSONRPCResponse<?>) each)
                .map(this::toServerSentEvent);
    }

    private JsonRpcTransportWrapper getJsonRpcTransportWrapper() {
        if (jsonRpcTransportWrapper == null) {
            jsonRpcTransportWrapper = agentScopeA2aServer.getTransportWrapper(
                    TransportProtocol.JSONRPC.asString(),
                    JsonRpcTransportWrapper.class);
        }
        return jsonRpcTransportWrapper;
    }

    private ServerSentEvent<String> toServerSentEvent(JSONRPCResponse<?> response) {
        try {
            String data = Utils.OBJECT_MAPPER.writeValueAsString(response);
            ServerSentEvent.Builder<String> builder =
                    ServerSentEvent.<String>builder()
                            .event("jsonrpc")
                            .data(data);
            if (response.getId() != null) {
                builder.id(response.getId().toString());
            }
            return builder.build();
        } catch (Exception exception) {
            log.error("A2A JSON-RPC 响应转换为 SSE 失败: {}", exception.getMessage(), exception);
            return ServerSentEvent.<String>builder()
                    .event("error")
                    .data("{\"error\":\"A2A response conversion failed\"}")
                    .build();
        }
    }

    /**
     * 强制使用服务端认证结果覆盖 A2A 请求中的 `message.metadata.userId`。
     *
     * <p>A2A 客户端不再需要也不应该自行传入 `userId/username`。这里把已经通过
     * API Key 认证的用户写回请求体，供 AgentScope 在内部构造 AgentRequestOptions
     * 时读取。若请求结构不是消息类方法，则保持原样透传。
     */
    private String injectAuthenticatedUser(String body, UserPrincipal principal) {
        if (principal == null || body == null || body.isBlank()) {
            return body;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (!(root instanceof ObjectNode rootObject)) {
                return body;
            }
            JsonNode paramsNode = rootObject.get("params");
            if (!(paramsNode instanceof ObjectNode paramsObject)) {
                return body;
            }
            JsonNode messageNode = paramsObject.get("message");
            if (!(messageNode instanceof ObjectNode messageObject)) {
                return body;
            }
            ObjectNode metadataObject =
                    messageObject.get("metadata") instanceof ObjectNode existingMetadata
                            ? existingMetadata
                            : messageObject.putObject("metadata");
            metadataObject.put("userId", String.valueOf(principal.userId()));
            metadataObject.put("username", principal.username());
            return objectMapper.writeValueAsString(rootObject);
        } catch (Exception exception) {
            log.warn(
                    "A2A 请求注入认证用户失败，将回退到原始请求体: userId={}, username={}, message={}",
                    principal.userId(),
                    principal.username(),
                    exception.getMessage());
            return body;
        }
    }
}
