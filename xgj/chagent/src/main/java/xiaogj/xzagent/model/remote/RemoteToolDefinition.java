package xiaogj.xzagent.model.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * 单个远程工具定义。
 *
 * @param name 工具名
 * @param description 工具说明
 * @param method HTTP 方法
 * @param path 相对路径
 * @param enabled 是否启用
 * @param riskLevel 风险等级
 * @param skillIds 推荐 skill 列表
 * @param baseUrl 工具级根地址覆盖
 * @param timeoutSeconds 工具级超时覆盖
 * @param headers 工具级附加请求头
 * @param auth 工具级认证配置
 * @param inputSchema 面向 Agent 的输入 JSON Schema
 * @param request 请求映射配置
 * @param response 响应映射配置
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteToolDefinition(
        String name,
        String description,
        String method,
        String path,
        Boolean enabled,
        String riskLevel,
        List<String> skillIds,
        String baseUrl,
        Integer timeoutSeconds,
        Map<String, String> headers,
        RemoteToolAuth auth,
        Map<String, Object> inputSchema,
        RemoteToolRequest request,
        RemoteToolResponse response) {
}

