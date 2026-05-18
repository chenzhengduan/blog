package xiaogj.xzagent.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.model.SkillLocalToolBinding;
import xiaogj.xzagent.model.SkillMcpBinding;
import xiaogj.xzagent.model.SkillRemoteToolBinding;
import xiaogj.xzagent.repository.SkillLocalToolBindingRepository;
import xiaogj.xzagent.repository.SkillMcpBindingRepository;
import xiaogj.xzagent.repository.SkillRemoteToolBindingRepository;
import xiaogj.xzagent.web.dto.SkillCapabilityResponse;
import org.springframework.stereotype.Service;

/**
 * Skill 能力总览服务。
 *
 * <p>当前前端设置页需要向用户明确展示三层关系：
 * Agent -> Skill -> 能力来源。
 * 这个服务专门把零散的绑定表聚合成面向设置页的只读视图，
 * 避免前端自己拼装多个接口后仍然看不清边界。
 */
@Service
public class SkillCapabilityService {

    private final SkillLocalToolBindingRepository skillLocalToolBindingRepository;
    private final SkillMcpBindingRepository skillMcpBindingRepository;
    private final SkillRemoteToolBindingRepository skillRemoteToolBindingRepository;

    public SkillCapabilityService(
            SkillLocalToolBindingRepository skillLocalToolBindingRepository,
            SkillMcpBindingRepository skillMcpBindingRepository,
            SkillRemoteToolBindingRepository skillRemoteToolBindingRepository) {
        this.skillLocalToolBindingRepository = skillLocalToolBindingRepository;
        this.skillMcpBindingRepository = skillMcpBindingRepository;
        this.skillRemoteToolBindingRepository = skillRemoteToolBindingRepository;
    }

    /**
     * 汇总当前全部 Skill 的能力接入关系。
     */
    public List<SkillCapabilityResponse> listCapabilities() {
        Map<String, SkillCapabilityAccumulator> capabilities = new LinkedHashMap<>();

        for (SkillLocalToolBinding binding : skillLocalToolBindingRepository.findEnabledBindings()) {
            capabilities.computeIfAbsent(binding.skillId(), SkillCapabilityAccumulator::new)
                    .localTools()
                    .add(binding.toolName());
        }

        for (SkillMcpBinding binding : skillMcpBindingRepository.findEnabledBindings()) {
            capabilities.computeIfAbsent(binding.skillId(), SkillCapabilityAccumulator::new)
                    .mcpServers()
                    .add(binding.serverId());
        }

        for (SkillRemoteToolBinding binding : skillRemoteToolBindingRepository.findEnabledBindings()) {
            capabilities.computeIfAbsent(binding.skillId(), SkillCapabilityAccumulator::new)
                    .remoteSources()
                    .add(binding.sourceId());
        }

        return capabilities.values().stream()
                .map(accumulator -> new SkillCapabilityResponse(
                        accumulator.skillId(),
                        List.copyOf(accumulator.localTools()),
                        List.copyOf(accumulator.mcpServers()),
                        List.copyOf(accumulator.remoteSources())))
                .toList();
    }

    /**
     * 查询单个 Skill 的能力关系。
     *
     * <p>当某个 Skill 暂时没有任何来源绑定时，也会返回一个空能力视图，
     * 这样前端在“按 Skill 保存绑定”后可以拿到稳定结构，不需要自己兜底补空对象。
     */
    public SkillCapabilityResponse getCapability(String skillId) {
        return listCapabilities().stream()
                .filter(capability -> capability.skillId().equals(skillId))
                .findFirst()
                .orElseGet(() -> new SkillCapabilityResponse(
                        skillId,
                        List.of(),
                        List.of(),
                        List.of()));
    }

    /**
     * 内部聚合对象。
     */
    private record SkillCapabilityAccumulator(
            String skillId,
            List<String> localTools,
            List<String> mcpServers,
            List<String> remoteSources) {

        private SkillCapabilityAccumulator(String skillId) {
            this(skillId, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }
}
