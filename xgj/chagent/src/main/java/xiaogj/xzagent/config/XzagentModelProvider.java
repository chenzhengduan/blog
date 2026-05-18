package xiaogj.xzagent.config;

/**
 * Xzagent 当前运行时使用的模型服务商。
 *
 * <p>枚举值会直接绑定 Spring 配置中的 `xzagent.model.provider`，
 * 因此保持小写命名，便于通过环境变量和 YAML 直观配置。
 */
public enum XzagentModelProvider {

    /**
     * 使用 Anthropic Claude 系列模型。
     */
    ANTHROPIC,

    /**
     * 使用 OpenAI 以及兼容 OpenAI Chat Completions API 的模型服务。
     */
    OPENAI
}
