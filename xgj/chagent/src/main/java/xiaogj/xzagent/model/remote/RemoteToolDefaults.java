package xiaogj.xzagent.model.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * 远程工具默认配置。
 *
 * @param baseUrl 默认根地址
 * @param timeoutSeconds 默认超时秒数
 * @param headers 默认请求头
 * @param auth 默认认证配置
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteToolDefaults(
        String baseUrl,
        Integer timeoutSeconds,
        Map<String, String> headers,
        RemoteToolAuth auth) {
}

