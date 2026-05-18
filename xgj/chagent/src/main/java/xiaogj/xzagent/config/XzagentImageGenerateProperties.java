package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 图像生成工具配置。
 *
 * <p>统一管理图像生成工具的上传相关配置，包括：
 * <ul>
 *     <li>上传接口地址：生成的图片会 POST 到该地址，由服务端统一管理，AI 不可见</li>
 *     <li>上传凭证 ID：引用用户的第三方凭证，工具运行时会使用该凭证的 HTTP 请求头进行鉴权</li>
 * </ul>
 *
 * <p>图像生成供应商（{@code provider}）通过 {@code xzagent.tools.image-generate.provider}
 * 配置，由 {@link ToolConfig} 中的 {@code @ConditionalOnProperty} 读取，不在本配置类中重复声明。
 */
@ConfigurationProperties(prefix = "xzagent.tools.image-generate")
public record XzagentImageGenerateProperties(

        /**
         * 图片上传接口地址（必填）。
         *
         * <p>工具生成图片后会先下载临时 URL 中的图片原始内容，
         * 再对该地址发起 POST 请求，上传原始文件流。
         * 上传成功后接口需返回 JSON 中的 {@code url} 字段（或纯文本 URL）作为持久化地址。
         */
        String uploadUrl,

        /**
         * 上传凭证 ID（可选）。
         *
         * <p>对应 {@code user_third_party_credential} 表中的 {@code credential_id}。
         * 配置后，工具会在上传请求中自动附加该凭证的 HTTP 请求头（如 Authorization 等）。
         * 不配置则不附加任何凭证请求头。
         */
        String uploadCredentialId) {
}
