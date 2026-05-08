package xiaogj.xzagent.model;

/**
 * Skill 与本地 Java Tool 的绑定关系。
 */
public record SkillLocalToolBinding(String skillId, String toolName, boolean enabled) {
}
