package xiaogj.xzagent.model;

/**
 * Skill 与远程工具绑定关系。
 *
 * <p>该绑定关系将“远程工具元数据标准”与现有 Skill 体系衔接起来。
 * 设计上不直接依赖元数据里的 `skillIds`，而是通过服务端持久化绑定
 * 作为最终生效规则，避免把安全边界完全交给远程配置源决定。
 *
 * @param skillId Skill 逻辑标识
 * @param sourceId 远程元数据源标识
 * @param toolName 工具名
 * @param enabled 是否启用
 */
public record SkillRemoteToolBinding(
        String skillId,
        String sourceId,
        String toolName,
        boolean enabled) {
}

