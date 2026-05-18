package xiaogj.xzagent.service;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.Toolkit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import xiaogj.xzagent.infrastructure.mcp.McpClientRegistry;
import xiaogj.xzagent.model.SkillLocalToolBinding;
import xiaogj.xzagent.model.SkillMcpBinding;
import xiaogj.xzagent.model.SkillRemoteToolBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 将 Skill 与本地 Tool / MCP Tool 的绑定关系装配到运行时。
 */
@Service
public class SkillBindingService {

    private static final Logger log = LoggerFactory.getLogger(SkillBindingService.class);

    private final McpClientRegistry mcpClientRegistry;
    private final List<AgentTool> builtInAgentTools;
    private final RemoteToolFactory remoteToolFactory;

    public SkillBindingService(
            McpClientRegistry mcpClientRegistry,
            List<AgentTool> builtInAgentTools,
            RemoteToolFactory remoteToolFactory) {
        this.mcpClientRegistry = mcpClientRegistry;
        this.builtInAgentTools = builtInAgentTools;
        this.remoteToolFactory = remoteToolFactory;
    }

    /**
     * 将快照中的 Skill 绑定关系应用到当前会话使用的 {@link SkillBox}。
     */
    public void applyBindings(
            RuntimeSnapshot snapshot,
            SkillBox skillBox,
            Toolkit toolkit,
            Long userId,
            String username) {
        Map<String, AgentSkill> skills = snapshot.skillLookup();
        Map<String, AgentTool> toolsByName = buildToolsByName(snapshot, userId, username);

        for (SkillLocalToolBinding binding : snapshot.localToolBindings()) {
            AgentSkill skill = resolveSkill(skills, binding.skillId());
            AgentTool tool = toolsByName.get(binding.toolName());
            if (skill == null || tool == null) {
                log.warn(
                        "跳过无效本地 Tool 绑定: bindingSkillId={}, resolvedSkillId={}, toolName={}, skillExists={}, toolExists= {}",
                        binding.skillId(),
                        skill != null ? skill.getSkillId() : null,
                        binding.toolName(),
                        skill != null,
                        tool != null);
                continue;
            }
            skillBox.registration().toolkit(toolkit).skill(skill).agentTool(tool).apply();
        }

        for (SkillRemoteToolBinding binding : snapshot.remoteToolBindings()) {
            AgentSkill skill = resolveSkill(skills, binding.skillId());
            AgentTool tool = toolsByName.get(binding.toolName());
            if (skill == null || tool == null) {
                log.warn(
                        "跳过无效远程 Tool 绑定: bindingSkillId={}, resolvedSkillId={}, sourceId={}, toolName={}, skillExists={}, toolExists={}",
                        binding.skillId(),
                        skill != null ? skill.getSkillId() : null,
                        binding.sourceId(),
                        binding.toolName(),
                        skill != null,
                        tool != null);
                continue;
            }
            skillBox.registration().toolkit(toolkit).skill(skill).agentTool(tool).apply();
        }

        for (SkillMcpBinding binding : snapshot.mcpBindings()) {
            AgentSkill skill = resolveSkill(skills, binding.skillId());
            if (skill == null) {
                log.warn("跳过无效 MCP 绑定，Skill 不存在: {}", binding.skillId());
                continue;
            }
            mcpClientRegistry.getClient(binding.serverId())
                    .ifPresentOrElse(
                            client -> skillBox.registration()
                                    .toolkit(toolkit)
                                    .skill(skill)
                                    .mcpClient(client)
                                    .enableTools(binding.enableTools())
                                    .disableTools(binding.disableTools())
                                    .apply(),
                            () -> log.warn("跳过 MCP 绑定，server 不可用: {}", binding.serverId()));
        }
    }

    private AgentSkill resolveSkill(Map<String, AgentSkill> skills, String bindingSkillId) {
        AgentSkill exact = skills.get(bindingSkillId);
        if (exact != null) {
            return exact;
        }
        return skills.values().stream()
                .filter(skill -> skill.getSkillId().equals(bindingSkillId))
                .findFirst()
                .orElse(null);
    }

    private Map<String, AgentTool> buildToolsByName(RuntimeSnapshot snapshot, Long userId, String username) {
        return Stream.concat(
                        builtInAgentTools.stream(),
                        remoteToolFactory.createEnabledTools(snapshot, userId, username).stream())
                .collect(Collectors.toMap(AgentTool::getName, Function.identity(), (left, right) -> right));
    }
}
