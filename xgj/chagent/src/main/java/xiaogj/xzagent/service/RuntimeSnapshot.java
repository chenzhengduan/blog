package xiaogj.xzagent.service;

import io.agentscope.core.skill.AgentSkill;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.model.AgentDefinition;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;
import xiaogj.xzagent.model.SkillLocalToolBinding;
import xiaogj.xzagent.model.SkillMcpBinding;
import xiaogj.xzagent.model.SkillRemoteToolBinding;
import xiaogj.xzagent.model.remote.RemoteToolMetaDocument;

public record RuntimeSnapshot(
        AgentDefinition agentDefinition,
        List<AgentSkill> skills,
        Map<String, AgentSkill> skillLookup,
        List<SkillLocalToolBinding> localToolBindings,
        List<SkillMcpBinding> mcpBindings,
        List<SkillRemoteToolBinding> remoteToolBindings,
        Map<String, RemoteToolSourceDefinition> enabledRemoteSourcesById,
        Map<String, RemoteToolMetaDocument> remoteMetaDocuments) {
}
