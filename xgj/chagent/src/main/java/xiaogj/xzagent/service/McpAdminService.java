package xiaogj.xzagent.service;

import java.time.Duration;
import java.util.List;
import xiaogj.xzagent.infrastructure.mcp.McpClientRegistry;
import xiaogj.xzagent.model.McpServerDefinition;
import xiaogj.xzagent.model.McpTransportType;
import xiaogj.xzagent.model.SkillMcpBinding;
import xiaogj.xzagent.repository.McpServerRepository;
import xiaogj.xzagent.repository.SkillMcpBindingRepository;
import xiaogj.xzagent.web.dto.McpServerBindingRequest;
import xiaogj.xzagent.web.dto.McpServerRequest;
import xiaogj.xzagent.web.dto.McpServerResponse;
import org.springframework.stereotype.Service;

/**
 * MCP 配置管理服务。
 */
@Service
public class McpAdminService {

    private final McpServerRepository mcpServerRepository;
    private final SkillMcpBindingRepository skillMcpBindingRepository;
    private final McpClientRegistry mcpClientRegistry;
    private final RuntimeSnapshotRegistry runtimeSnapshotRegistry;

    public McpAdminService(
            McpServerRepository mcpServerRepository,
            SkillMcpBindingRepository skillMcpBindingRepository,
            McpClientRegistry mcpClientRegistry,
            RuntimeSnapshotRegistry runtimeSnapshotRegistry) {
        this.mcpServerRepository = mcpServerRepository;
        this.skillMcpBindingRepository = skillMcpBindingRepository;
        this.mcpClientRegistry = mcpClientRegistry;
        this.runtimeSnapshotRegistry = runtimeSnapshotRegistry;
    }

    /**
     * 列出当前全部 MCP Server 配置。
     */
    public List<McpServerResponse> listServers() {
        return mcpServerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 保存 MCP Server 并同步 Skill 绑定，然后刷新运行时客户端。
     */
    public McpServerResponse saveServer(McpServerRequest request) {
        McpServerDefinition definition = new McpServerDefinition(
                request.serverId(),
                request.name(),
                McpTransportType.valueOf(request.transportType()),
                request.enabled(),
                request.config(),
                request.headers(),
                request.queryParams(),
                Duration.ofSeconds(request.timeoutSeconds()),
                Duration.ofSeconds(request.initializationTimeoutSeconds()));
        mcpServerRepository.upsert(definition);
        skillMcpBindingRepository.replaceByServerId(
                request.serverId(),
                request.skillBindings().stream()
                        .map(this::toBinding)
                        .toList());
        mcpClientRegistry.refreshServer(definition);
        runtimeSnapshotRegistry.refresh();
        return toResponse(definition);
    }

    private SkillMcpBinding toBinding(McpServerBindingRequest request) {
        return new SkillMcpBinding(
                request.skillId(),
                request.serverId(),
                request.enableTools(),
                request.disableTools(),
                request.enabled());
    }

    private McpServerResponse toResponse(McpServerDefinition definition) {
        List<McpServerBindingRequest> bindings = skillMcpBindingRepository.findByServerId(
                        definition.serverId()).stream()
                .map(binding -> new McpServerBindingRequest(
                        binding.skillId(),
                        binding.serverId(),
                        binding.enableTools(),
                        binding.disableTools(),
                        binding.enabled()))
                .toList();
        return new McpServerResponse(
                definition.serverId(),
                definition.name(),
                definition.transportType().name(),
                definition.enabled(),
                definition.config(),
                definition.headers(),
                definition.queryParams(),
                definition.timeout().toSeconds(),
                definition.initializationTimeout().toSeconds(),
                bindings);
    }
}
