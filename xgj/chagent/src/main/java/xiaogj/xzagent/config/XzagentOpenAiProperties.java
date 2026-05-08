package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * OpenAI 及 OpenAI API 兼容模型接入配置。
 *
 * @param apiKey        OpenAI 或兼容服务访问密钥
 * @param modelName     默认对话模型名称
 * @param streamEnabled 是否启用流式返回
 * @param baseUrl       可选模型服务地址，便于接入代理、私有网关或 DeepSeek/vLLM 等兼容服务
 * @param imageModel    图像生成模型名称，如 dall-e-3；为空时使用默认值
 */
@Validated
@ConfigurationProperties(prefix = "xzagent.openai")
public record XzagentOpenAiProperties(
        String apiKey,
        String modelName,
        boolean streamEnabled,
        String baseUrl,
        String imageModel) {

    /** 默认图像生成模型。 */
    public static final String DEFAULT_IMAGE_MODEL = "dall-e-3";

    /**
     * 返回图像生成实际使用的模型名称，缺失时回退到默认值。
     */
    public String resolvedImageModel() {
        return imageModel != null && !imageModel.isBlank() ? imageModel : DEFAULT_IMAGE_MODEL;
    }
}
