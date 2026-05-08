package xiaogj.xzagent.infrastructure.mcp;

import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import java.time.Duration;
import xiaogj.xzagent.model.McpServerDefinition;
import xiaogj.xzagent.model.McpTransportType;
import xiaogj.xzagent.repository.McpServerRepository;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class McpClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpClientRegistry.class);

    private final McpServerRepository repository;
    private final Map<String, McpClientWrapper> clients = new ConcurrentHashMap<>();

    public McpClientRegistry(McpServerRepository repository) {
        this.repository = repository;
    }

    /**
     * 启动时加载所有启用状态的 MCP Client。
     *
     * <p>这里故意采用“单个失败不影响整体”的策略，防止某个不稳定的
     * MCP Server 阻断整个应用启动。
     */
    @PostConstruct
    public void initialize() {
        for (McpServerDefinition definition : repository.findEnabledServers()) {
            try {
                clients.put(definition.serverId(), build(definition));
                log.info("已加载 MCP server: {}", definition.serverId());
            } catch (Exception e) {
                log.warn("MCP server 加载失败: {}", definition.serverId(), e);
            }
        }
    }

    /**
     * 按 serverId 查询已经初始化成功的 MCP Client。
     */
    public Optional<McpClientWrapper> getClient(String serverId) {
        return Optional.ofNullable(clients.get(serverId));
    }

    /**
     * 根据最新配置刷新单个 server 的客户端实例。
     *
     * <p>启用状态的 server 会被重建并覆盖旧实例；禁用状态的 server 会被
     * 从注册表移除。这样管理接口保存配置后，无需重启应用即可生效。
     */
    public void refreshServer(McpServerDefinition definition) {
        clients.remove(definition.serverId());
        if (!definition.enabled()) {
            log.info("已移除 MCP server: {}", definition.serverId());
            return;
        }
        try {
            clients.put(definition.serverId(), build(definition));
            log.info("已刷新 MCP server: {}", definition.serverId());
        } catch (Exception e) {
            log.warn("刷新 MCP server 失败: {}", definition.serverId(), e);
        }
    }

    /**
     * 返回只读视图，便于诊断当前已加载的 MCP Client。
     */
    public Map<String, McpClientWrapper> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    private McpClientWrapper build(McpServerDefinition definition) {
        McpClientBuilder builder = McpClientBuilder.create(definition.serverId());
        switch (definition.transportType()) {
            case STDIO -> {
                Object command = definition.config().get("command");
                Object args = definition.config().get("args");
                // STDIO 方式需要把 args 还原成字符串数组，便于底层进程启动。
                String[] commandArgs = args instanceof Iterable<?> iterable
                        ? toStringArray(iterable)
                        : new String[0];
                builder.stdioTransport(String.valueOf(command), commandArgs);
            }
            case SSE -> builder.sseTransport(String.valueOf(definition.config().get("endpoint")));
            case HTTP -> builder.streamableHttpTransport(String.valueOf(definition.config().get("endpoint")));
            default -> throw new IllegalArgumentException("不支持的 MCP transport: " + definition.transportType());
        }
        definition.headers().forEach(builder::header);
        definition.queryParams().forEach(builder::queryParam);
        if (definition.timeout() != null) {
            builder.timeout(definition.timeout());
        }
        if (definition.initializationTimeout() != null) {
            builder.initializationTimeout(definition.initializationTimeout());
        }
        return builder.buildAsync().block();
    }

    private String[] toStringArray(Iterable<?> iterable) {
        return java.util.stream.StreamSupport.stream(iterable.spliterator(), false)
                .map(String::valueOf)
                .toArray(String[]::new);
    }
}
