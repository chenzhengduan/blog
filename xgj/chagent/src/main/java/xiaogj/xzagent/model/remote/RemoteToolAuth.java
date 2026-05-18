package xiaogj.xzagent.model.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 远程工具认证配置。
 *
 * @param type 认证类型
 * @param tokenEnv token 对应的环境变量名
 * @param headerName API Key 场景使用的请求头名
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteToolAuth(
        String type,
        String tokenEnv,
        String headerName) {
}

