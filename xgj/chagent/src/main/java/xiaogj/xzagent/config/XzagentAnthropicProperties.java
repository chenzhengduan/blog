package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Anthropic 模型接入配置。
 *
 * @param apiKey Anthropic 访问密钥
 * @param modelName 默认模型名称
 * @param streamEnabled 是否启用流式返回
 * @param baseUrl 可选的模型服务地址，便于走代理或私有网关
 */
@Validated
@ConfigurationProperties(prefix = "xzagent.anthropic")
public record XzagentAnthropicProperties(
        String apiKey,
        String modelName,
        boolean streamEnabled,
        String baseUrl) {
}
