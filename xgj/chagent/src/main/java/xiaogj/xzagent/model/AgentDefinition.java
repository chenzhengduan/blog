package xiaogj.xzagent.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Agent 定义。
 *
 * <p>当前阶段系统只支持一个可配置 Agent，但仍然显式建表建模，
 * 是为了把"运行时默认值"和"数据库可配置值"分离开。这样后续扩展
 * 到多 Agent 时，不需要推翻现有数据结构，只需要放开数量限制和绑定关系。
 *
 * @param agentId Agent 唯一标识
 * @param name Agent 名称
 * @param systemPrompt Agent 基础系统提示词
 * @param agentsMd AGENTS.md 扩展提示词，追加在 systemPrompt 之后；可为 null
 * @param maxIterations 单轮最大推理迭代数
 * @param skillIds 当前 Agent 允许使用的 Skill 列表
 * @param enabled 是否启用
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record AgentDefinition(
        String agentId,
        String name,
        String systemPrompt,
        String agentsMd,
        int maxIterations,
        List<String> skillIds,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt) {

    /** 当前时间注入格式：yyyy-MM-dd HH:mm（Asia/Shanghai）。 */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Shanghai"));

    /**
     * 返回运行时实际生效的系统提示词。
     *
     * <p>在每次请求时动态构建，依次追加：
     * <ol>
     *   <li>当前时间（Asia/Shanghai），让 Agent 无需工具即可感知基准时间</li>
     *   <li>agentsMd 扩展提示词（若非空），类似 CLAUDE.md 的叠加机制</li>
     * </ol>
     */
    public String effectiveSystemPrompt() {
        String timePrefix = "当前时间：" + TIME_FORMATTER.format(Instant.now()) + "\n";
        String base = timePrefix + systemPrompt;
        if (agentsMd == null || agentsMd.isBlank()) {
            return base;
        }
        return base + "\n" + agentsMd;
    }
}
