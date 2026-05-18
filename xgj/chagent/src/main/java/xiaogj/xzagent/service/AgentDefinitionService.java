package xiaogj.xzagent.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.AgentDefinition;
import xiaogj.xzagent.repository.AgentDefinitionRepository;
import xiaogj.xzagent.web.dto.AgentDefinitionRequest;
import xiaogj.xzagent.web.dto.AgentDefinitionResponse;
import org.springframework.stereotype.Service;

/**
 * Agent 定义应用服务。
 *
 * <p>当前只支持一个可配置 Agent，因此该服务暴露的是“读取当前配置”和
 * “覆盖当前配置”的接口，而不是多 Agent 管理模型。
 */
@Service
public class AgentDefinitionService {

    private static final String DEFAULT_AGENT_ID = "default";

    private final AgentDefinitionRepository agentDefinitionRepository;
    private final RuntimeSnapshotRegistry runtimeSnapshotRegistry;

    public AgentDefinitionService(
            AgentDefinitionRepository agentDefinitionRepository,
            RuntimeSnapshotRegistry runtimeSnapshotRegistry) {
        this.agentDefinitionRepository = agentDefinitionRepository;
        this.runtimeSnapshotRegistry = runtimeSnapshotRegistry;
    }

    /**
     * 查询当前运行时应使用的 Agent 定义。
     */
    public AgentDefinition getRuntimeDefinition() {
        return runtimeSnapshotRegistry.getSnapshot().agentDefinition();
    }

    /**
     * 查询当前 Agent 配置，供管理接口展示。
     */
    public AgentDefinitionResponse getCurrentDefinition() {
        AgentDefinition definition = getRuntimeDefinition();
        return new AgentDefinitionResponse(
                definition.agentId(),
                definition.name(),
                definition.systemPrompt(),
                definition.agentsMd(),
                definition.maxIterations(),
                definition.skillIds(),
                definition.enabled(),
                definition.createdAt(),
                definition.updatedAt());
    }

    /**
     * 保存当前唯一 Agent 的配置。
     */
    public AgentDefinitionResponse saveDefinition(AgentDefinitionRequest request) {
        Instant now = Instant.now();
        AgentDefinition definition = new AgentDefinition(
                Optional.ofNullable(request.agentId()).filter(value -> !value.isBlank()).orElse(DEFAULT_AGENT_ID),
                request.name(),
                request.systemPrompt(),
                request.agentsMd(),
                request.maxIterations(),
                normalizeSkillIds(request.skillIds()),
                request.enabled(),
                now,
                now);
        agentDefinitionRepository.upsert(definition);
        runtimeSnapshotRegistry.refresh();
        return getCurrentDefinition();
    }

    /**
     * 更新当前 Agent 的 AGENTS.md 扩展提示词。
     *
     * <p>保留当前配置的其余字段不变，仅覆盖 agentsMd。
     */
    public void updateAgentsMd(String agentsMd) {
        AgentDefinition current = getRuntimeDefinition();
        Instant now = Instant.now();
        agentDefinitionRepository.upsert(new AgentDefinition(
                current.agentId(),
                current.name(),
                current.systemPrompt(),
                agentsMd,
                current.maxIterations(),
                current.skillIds(),
                current.enabled(),
                current.createdAt() != null ? current.createdAt() : now,
                now));
        runtimeSnapshotRegistry.refresh();
    }

    private List<String> normalizeSkillIds(List<String> requestedSkillIds) {
        List<String> availableSkillIds = runtimeSnapshotRegistry.getSnapshot().skills().stream()
                .map(skill -> skill.getName())
                .toList();
        if (requestedSkillIds == null || requestedSkillIds.isEmpty()) {
            return availableSkillIds;
        }
        return requestedSkillIds.stream()
                .filter(skillId -> skillId != null && !skillId.isBlank())
                .filter(availableSkillIds::contains)
                .distinct()
                .toList();
    }
}
