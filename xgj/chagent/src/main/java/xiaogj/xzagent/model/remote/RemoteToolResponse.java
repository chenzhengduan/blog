package xiaogj.xzagent.model.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 远程工具响应映射定义。
 *
 * @param contentType 响应内容类型
 * @param mapping 响射配置
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteToolResponse(
        String contentType,
        RemoteToolResponseMapping mapping) {
}

