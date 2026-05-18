package xiaogj.xzagent.model.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * 远程工具请求映射定义。
 *
 * @param pathParams 路径参数映射
 * @param queryParams 查询参数映射
 * @param headers 额外请求头
 * @param bodyTemplate 请求体模板
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteToolRequest(
        Map<String, String> pathParams,
        Map<String, String> queryParams,
        Map<String, String> headers,
        Map<String, Object> bodyTemplate) {
}

