package xiaogj.xzagent.service;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import xiaogj.xzagent.config.XzagentAgentProperties;
import xiaogj.xzagent.model.AgentDefinition;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;
import xiaogj.xzagent.repository.AgentDefinitionRepository;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import xiaogj.xzagent.repository.SkillLocalToolBindingRepository;
import xiaogj.xzagent.repository.SkillMcpBindingRepository;
import xiaogj.xzagent.repository.SkillRemoteToolBindingRepository;
import org.springframework.stereotype.Component;

@Component
public class RuntimeSnapshotRegistry {

    private static final String DEFAULT_AGENT_ID = "default";

    private final AgentDefinitionRepository agentDefinitionRepository;
    private final XzagentAgentProperties defaultProperties;
    private final AgentSkillRepository skillRepository;
    private final SkillLocalToolBindingRepository skillLocalToolBindingRepository;
    private final SkillMcpBindingRepository skillMcpBindingRepository;
    private final SkillRemoteToolBindingRepository skillRemoteToolBindingRepository;
    private final RemoteToolSourceRepository remoteToolSourceRepository;
    private final RemoteToolMetaService remoteToolMetaService;

    private volatile RuntimeSnapshot snapshot;

    public RuntimeSnapshotRegistry(
            AgentDefinitionRepository agentDefinitionRepository,
            XzagentAgentProperties defaultProperties,
            AgentSkillRepository skillRepository,
            SkillLocalToolBindingRepository skillLocalToolBindingRepository,
            SkillMcpBindingRepository skillMcpBindingRepository,
            SkillRemoteToolBindingRepository skillRemoteToolBindingRepository,
            RemoteToolSourceRepository remoteToolSourceRepository,
            RemoteToolMetaService remoteToolMetaService) {
        this.agentDefinitionRepository = agentDefinitionRepository;
        this.defaultProperties = defaultProperties;
        this.skillRepository = skillRepository;
        this.skillLocalToolBindingRepository = skillLocalToolBindingRepository;
        this.skillMcpBindingRepository = skillMcpBindingRepository;
        this.skillRemoteToolBindingRepository = skillRemoteToolBindingRepository;
        this.remoteToolSourceRepository = remoteToolSourceRepository;
        this.remoteToolMetaService = remoteToolMetaService;
    }

    @PostConstruct
    public void initialize() {
        refresh();
    }

    public RuntimeSnapshot getSnapshot() {
        RuntimeSnapshot current = snapshot;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (snapshot == null) {
                snapshot = buildSnapshot();
            }
            return snapshot;
        }
    }

    public synchronized RuntimeSnapshot refresh() {
        snapshot = buildSnapshot();
        return snapshot;
    }

    private RuntimeSnapshot buildSnapshot() {
        List<AgentSkill> skills = List.copyOf(skillRepository.getAllSkills());
        Map<String, AgentSkill> skillLookup = skills.stream()
                .flatMap(skill -> Stream.of(
                        Map.entry(skill.getName(), skill),
                        Map.entry(skill.getSkillId(), skill)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left));
        AgentDefinition agentDefinition = agentDefinitionRepository.findActive()
                .map(definition -> normalizeDefinition(definition, skills))
                .orElseGet(() -> defaultDefinition(skills));
        List<RemoteToolSourceDefinition> enabledRemoteSources = remoteToolSourceRepository.findEnabledSources();
        Map<String, RemoteToolSourceDefinition> enabledRemoteSourcesById = enabledRemoteSources.stream()
                .collect(Collectors.toMap(
                        RemoteToolSourceDefinition::sourceId,
                        source -> source,
                        (left, right) -> right,
                        LinkedHashMap::new));
        return new RuntimeSnapshot(
                agentDefinition,
                skills,
                Map.copyOf(skillLookup),
                List.copyOf(skillLocalToolBindingRepository.findEnabledBindings()),
                List.copyOf(skillMcpBindingRepository.findEnabledBindings()),
                List.copyOf(skillRemoteToolBindingRepository.findEnabledBindings()),
                Map.copyOf(enabledRemoteSourcesById),
                Map.copyOf(remoteToolMetaService.loadMetaDocuments(enabledRemoteSources)));
    }

    private AgentDefinition defaultDefinition(List<AgentSkill> skills) {
        Instant now = Instant.now();
        return new AgentDefinition(
                DEFAULT_AGENT_ID,
                defaultProperties.name(),
                defaultProperties.systemPrompt(),
                null,
                defaultProperties.maxIterations(),
                normalizeSkillIds(List.of(), skills),
                true,
                now,
                now);
    }

    private AgentDefinition normalizeDefinition(AgentDefinition definition, List<AgentSkill> skills) {
        return new AgentDefinition(
                definition.agentId(),
                definition.name(),
                definition.systemPrompt(),
                definition.agentsMd(),
                definition.maxIterations(),
                normalizeSkillIds(definition.skillIds(), skills),
                definition.enabled(),
                definition.createdAt(),
                definition.updatedAt());
    }

    private List<String> normalizeSkillIds(List<String> requestedSkillIds, List<AgentSkill> skills) {
        List<String> availableSkillIds = skills.stream()
                .map(AgentSkill::getName)
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
