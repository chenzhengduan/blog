package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DashScope（阿里云百炼）接入配置。
 *
 * <p>当前主要用于万相系列图像生成模型（wan2.7-image-pro、wan2.7-image）。
 * API Key 可在阿里云百炼控制台获取：https://bailian.console.aliyun.com/
 *
 * @param apiKey DashScope API 密钥，对应环境变量 DASHSCOPE_API_KEY
 * @param imageModel 图像生成工具默认模型，对应配置项 {@code xzagent.dashscope.image-model}
 */
@ConfigurationProperties(prefix = "xzagent.dashscope")
public record XzagentDashScopeProperties(String apiKey, String imageModel) {

    /** 默认生图模型。 */
    public static final String DEFAULT_IMAGE_MODEL = "wan2.7-image";

    /**
     * 判断 DashScope API Key 是否已配置。
     *
     * @return true 表示 API Key 已设置且不为空白
     */
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * 返回图像生成工具实际使用的默认模型。
     *
     * <p>当配置缺失或为空白时，回退到平台内置默认值。
     */
    public String resolvedImageModel() {
        return imageModel != null && !imageModel.isBlank() ? imageModel : DEFAULT_IMAGE_MODEL;
    }
}
