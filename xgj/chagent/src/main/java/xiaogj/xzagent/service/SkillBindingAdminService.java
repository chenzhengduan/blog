package xiaogj.xzagent.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import io.agentscope.core.tool.AgentTool;
import xiaogj.xzagent.model.McpServerDefinition;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;
import xiaogj.xzagent.model.SkillLocalToolBinding;
import xiaogj.xzagent.model.SkillMcpBinding;
import xiaogj.xzagent.model.SkillRemoteToolBinding;
import xiaogj.xzagent.model.remote.RemoteToolMetaDocument;
import xiaogj.xzagent.repository.McpServerRepository;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import xiaogj.xzagent.repository.SkillLocalToolBindingRepository;
import xiaogj.xzagent.repository.SkillMcpBindingRepository;
import xiaogj.xzagent.repository.SkillRemoteToolBindingRepository;
import xiaogj.xzagent.web.dto.SkillBindingUpdateRequest;
import xiaogj.xzagent.web.dto.SkillCapabilityResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Skill 绑定管理服务。
 *
 * <p>设置页现在采用 Skill 视角编排能力，因此这里提供一个专门的应用服务，
 * 负责把“勾选了哪些全局 MCP / 远程来源”翻译成底层绑定表。这样前端不需要
 * 直接理解 `skill_mcp_binding` 和 `skill_remote_tool_binding` 的细节。
 */
@Service
public class SkillBindingAdminService {

    private final McpServerRepository mcpServerRepository;
    private final RemoteToolSourceRepository remoteToolSourceRepository;
    private final SkillLocalToolBindingRepository skillLocalToolBindingRepository;
    private final SkillMcpBindingRepository skillMcpBindingRepository;
    private final SkillRemoteToolBindingRepository skillRemoteToolBindingRepository;
    private final RemoteToolMetaService remoteToolMetaService;
    private final RuntimeSnapshotRegistry runtimeSnapshotRegistry;
    private final SkillCapabilityService skillCapabilityService;
    private final List<AgentTool> builtInAgentTools;

    public SkillBindingAdminService(
            McpServerRepository mcpServerRepository,
            RemoteToolSourceRepository remoteToolSourceRepository,
            SkillLocalToolBindingRepository skillLocalToolBindingRepository,
            SkillMcpBindingRepository skillMcpBindingRepository,
            SkillRemoteToolBindingRepository skillRemoteToolBindingRepository,
            RemoteToolMetaService remoteToolMetaService,
            RuntimeSnapshotRegistry runtimeSnapshotRegistry,
            SkillCapabilityService skillCapabilityService,
            ObjectProvider<List<AgentTool>> builtInAgentToolsProvider) {
        this.mcpServerRepository = mcpServerRepository;
        this.remoteToolSourceRepository = remoteToolSourceRepository;
        this.skillLocalToolBindingRepository = skillLocalToolBindingRepository;
        this.skillMcpBindingRepository = skillMcpBindingRepository;
        this.skillRemoteToolBindingRepository = skillRemoteToolBindingRepository;
        this.remoteToolMetaService = remoteToolMetaService;
        this.runtimeSnapshotRegistry = runtimeSnapshotRegistry;
        this.skillCapabilityService = skillCapabilityService;
        this.builtInAgentTools = builtInAgentToolsProvider.getIfAvailable(List::of);
    }

    /**
     * 以 Skill 视角覆盖全局来源绑定。
     *
     * <p>MCP 绑定会尽量保留原有的工具白名单和黑名单；远程来源绑定则会根据
     * 元数据文档重新展开为该 source 下的全部启用工具，确保配置结果和来源文档
     * 保持一致。
     */
    public SkillCapabilityResponse updateBindings(String skillId, SkillBindingUpdateRequest request) {
        List<SkillLocalToolBinding> nextLocalBindings = new ArrayList<>();
        for (String toolName : distinctIds(request.localToolNames())) {
            validateLocalToolExists(toolName);
            nextLocalBindings.add(new SkillLocalToolBinding(skillId, toolName, true));
        }
        skillLocalToolBindingRepository.replaceBySkillId(skillId, nextLocalBindings);

        Map<String, SkillMcpBinding> currentMcpBindings = skillMcpBindingRepository.findBySkillId(skillId).stream()
                .collect(Collectors.toMap(SkillMcpBinding::serverId, Function.identity()));
        List<SkillMcpBinding> nextMcpBindings = new ArrayList<>();
        for (String serverId : distinctIds(request.mcpServerIds())) {
            McpServerDefinition server = findServer(serverId);
            SkillMcpBinding existing = currentMcpBindings.get(server.serverId());
            nextMcpBindings.add(new SkillMcpBinding(
                    skillId,
                    server.serverId(),
                    existing != null ? existing.enableTools() : List.of(),
                    existing != null ? existing.disableTools() : List.of(),
                    true));
        }
        skillMcpBindingRepository.replaceBySkillId(skillId, nextMcpBindings);

        List<SkillRemoteToolBinding> nextRemoteBindings = new ArrayList<>();
        for (String sourceId : distinctIds(request.remoteSourceIds())) {
            RemoteToolSourceDefinition source = findSource(sourceId);
            RemoteToolMetaDocument document = remoteToolMetaService.loadMetaDocument(source);
            if (document.tools() == null) {
                continue;
            }
            document.tools().stream()
                    .filter(tool -> tool.name() != null && !tool.name().isBlank())
                    .filter(tool -> tool.enabled() == null || Boolean.TRUE.equals(tool.enabled()))
                    .forEach(tool -> nextRemoteBindings.add(
                            new SkillRemoteToolBinding(skillId, sourceId, tool.name(), true)));
        }
        skillRemoteToolBindingRepository.replaceBySkillId(skillId, nextRemoteBindings);
        runtimeSnapshotRegistry.refresh();

        return skillCapabilityService.getCapability(skillId);
    }

    private List<String> distinctIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(ids.stream()
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private McpServerDefinition findServer(String serverId) {
        return mcpServerRepository.findAll().stream()
                .filter(server -> server.serverId().equals(serverId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到 MCP 服务: " + serverId));
    }

    private RemoteToolSourceDefinition findSource(String sourceId) {
        return remoteToolSourceRepository.findAll().stream()
                .filter(source -> source.sourceId().equals(sourceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到远程来源: " + sourceId));
    }

    private void validateLocalToolExists(String toolName) {
        boolean exists = builtInAgentTools.stream()
                .anyMatch(tool -> tool.getName().equals(toolName));
        if (!exists) {
            throw new IllegalArgumentException("未找到本地工具: " + toolName);
        }
    }
}
