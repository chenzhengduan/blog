package xiaogj.xzagent.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Xzagent 安全相关配置。
 *
 * <p>第一版账号体系只使用 access token，因此这里集中管理 JWT 和用户
 * 私有请求头加密所需的核心配置，避免散落在多个服务里各自读取环境变量。
 */
@ConfigurationProperties(prefix = "xzagent.security")
public record XzagentSecurityProperties(
        String jwtSecret,
        long jwtExpireMinutes,
        String headerEncryptionPassword,
        String headerEncryptionSalt,
        String apiKeyPrefix,
        int apiKeyLookupPrefixLength,
        Cors cors) {

    public record Cors(
            boolean enabled,
            List<String> allowedOrigins,
            List<String> allowedOriginPatterns,
            List<String> allowedMethods,
            List<String> allowedHeaders,
            List<String> exposedHeaders,
            boolean allowCredentials,
            long maxAgeSeconds) {

        public Cors {
            allowedOrigins = allowedOrigins != null ? List.copyOf(allowedOrigins) : List.of();
            allowedOriginPatterns = allowedOriginPatterns != null
                    ? List.copyOf(allowedOriginPatterns)
                    : List.of();
            allowedMethods = allowedMethods != null ? List.copyOf(allowedMethods) : List.of();
            allowedHeaders = allowedHeaders != null ? List.copyOf(allowedHeaders) : List.of();
            exposedHeaders = exposedHeaders != null ? List.copyOf(exposedHeaders) : List.of();
        }
    }
}
