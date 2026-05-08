package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTML 截图工具配置。
 *
 * <p>配置项：
 * <ul>
 *     <li>{@code enabled}：是否启用截图工具，默认 false</li>
 *     <li>{@code uploadUrl}：截图文件的上传接口地址，由服务端统一管理，AI 不可见</li>
 *     <li>{@code uploadCredentialId}：上传凭证 ID，引用用户的第三方凭证</li>
 * </ul>
 *
 * <p>这样既能避免在所有部署环境默认暴露浏览器截图能力，也方便后续按环境开关。
 */
@ConfigurationProperties(prefix = "xzagent.tools.html-screenshot")
public record XzagentHtmlScreenshotProperties(

        /** 是否启用 HTML 截图工具。 */
        boolean enabled,

        /**
         * 截图上传接口地址（必填，启用时）。
         *
         * <p>工具生成 PNG 截图后，会对该地址发起 POST 请求上传原始文件流。
         * 上传成功后接口需返回 JSON 中的 {@code url} 字段（或纯文本 URL）。
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
