package xiaogj.xzagent.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Xzagent 模型运行时总开关配置。
 *
 * @param provider 当前启用的模型服务商
 */
@Validated
@ConfigurationProperties(prefix = "xzagent.model")
public record XzagentModelProperties(
        @NotNull XzagentModelProvider provider) {
}
