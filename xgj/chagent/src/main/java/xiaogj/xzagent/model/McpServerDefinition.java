package xiaogj.xzagent.model;

import java.time.Duration;
import java.util.Map;

/**
 * MCP Server 持久化配置在内存中的投影。
 *
 * @param serverId 服务唯一标识
 * @param name 服务展示名
 * @param transportType 传输协议
 * @param enabled 是否启用
 * @param config 协议相关配置，例如 command、args、endpoint
 * @param headers 请求头
 * @param queryParams 查询参数
 * @param timeout 请求超时
 * @param initializationTimeout 初始化超时
 */
public record McpServerDefinition(
        String serverId,
        String name,
        McpTransportType transportType,
        boolean enabled,
        Map<String, Object> config,
        Map<String, String> headers,
        Map<String, String> queryParams,
        Duration timeout,
        Duration initializationTimeout) {
}
