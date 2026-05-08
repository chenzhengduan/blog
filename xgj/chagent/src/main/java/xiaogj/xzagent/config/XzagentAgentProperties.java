package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Agent 运行时基础配置。
 *
 * @param name Agent 在对话和日志中的名称
 * @param systemPrompt 全局系统提示词
 * @param maxIterations 单轮推理最大迭代次数
 */
@ConfigurationProperties(prefix = "xzagent.agent")
public record XzagentAgentProperties(
        String name,
        String systemPrompt,
        int maxIterations) {
}
