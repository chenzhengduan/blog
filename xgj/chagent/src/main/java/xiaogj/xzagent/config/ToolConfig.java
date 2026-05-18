package xiaogj.xzagent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.AgentTool;
import xiaogj.xzagent.repository.ImageGenerateUsageRepository;
import xiaogj.xzagent.service.ThirdPartyCredentialService;
import xiaogj.xzagent.tool.HtmlScreenshotTool;
import xiaogj.xzagent.tool.ImageGenerateTool;
import xiaogj.xzagent.tool.RenderA2uiTool;
import xiaogj.xzagent.tool.provider.DashScopeImageGenerateProvider;
import xiaogj.xzagent.tool.provider.ImageGenerateProvider;
import xiaogj.xzagent.tool.provider.OpenAiImageGenerateProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    /**
     * 注册标准 A2UI 输出工具。
     */
    @Bean
    public RenderA2uiTool renderA2uiTool(ObjectMapper objectMapper) {
        return new RenderA2uiTool(objectMapper);
    }

    /**
     * 注册 HTML 截图工具。
     *
     * <p>仅在配置 {@code xzagent.tools.html-screenshot.enabled=true} 时创建。
     * 上传地址与第三方凭证由服务端配置提供，对 AI 不可见。
     */
    @Bean
    @ConditionalOnProperty(prefix = "xzagent.tools.html-screenshot", name = "enabled", havingValue = "true")
    public AgentTool htmlScreenshotTool(
            XzagentHtmlScreenshotProperties properties,
            ObjectMapper objectMapper,
            ThirdPartyCredentialService credentialService) {
        return new HtmlScreenshotTool(properties, objectMapper, credentialService);
    }

    /**
     * 注册 DashScope 图像生成供应商。
     *
     * <p>仅在 {@code xzagent.tools.image-generate.provider=dashscope} 时创建。
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "xzagent.tools.image-generate",
            name = "provider",
            havingValue = "dashscope")
    public ImageGenerateProvider dashScopeImageGenerateProvider(
            XzagentDashScopeProperties dashScopeProperties,
            ObjectMapper objectMapper) {
        return new DashScopeImageGenerateProvider(
                dashScopeProperties.apiKey(),
                dashScopeProperties.resolvedImageModel(),
                objectMapper);
    }

    /**
     * 注册 OpenAI 图像生成供应商。
     *
     * <p>仅在 {@code xzagent.tools.image-generate.provider=openai} 时创建。
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "xzagent.tools.image-generate",
            name = "provider",
            havingValue = "openai")
    public ImageGenerateProvider openAiImageGenerateProvider(
            XzagentOpenAiProperties openAiProperties,
            ObjectMapper objectMapper) {
        return new OpenAiImageGenerateProvider(
                openAiProperties.apiKey(),
                openAiProperties.resolvedImageModel(),
                openAiProperties.baseUrl(),
                objectMapper);
    }

    /**
     * 注册图像生成工具。
     *
     * <p>仅在容器中存在 {@link ImageGenerateProvider} Bean 时创建，
     * 即配置了 {@code xzagent.tools.image-generate.provider} 时自动启用。
     * 上传地址与第三方凭证由服务端配置提供，对 AI 不可见。
     */
    @Bean
    @ConditionalOnBean(ImageGenerateProvider.class)
    public AgentTool imageGenerateTool(
            ImageGenerateProvider imageGenerateProvider,
            ObjectMapper objectMapper,
            XzagentImageGenerateProperties imageGenerateProperties,
            ThirdPartyCredentialService credentialService,
            ImageGenerateUsageRepository imageGenerateUsageRepository) {
        return new ImageGenerateTool(
                imageGenerateProvider,
                objectMapper,
                imageGenerateProperties,
                credentialService,
                imageGenerateUsageRepository);
    }
}
