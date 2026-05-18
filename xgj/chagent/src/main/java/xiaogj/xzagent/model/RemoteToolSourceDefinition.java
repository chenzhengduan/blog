package xiaogj.xzagent.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * 远程工具元数据源定义。
 *
 * <p>该对象对应数据库中的 `remote_tool_source` 表，描述一份远程或本地
 * 元数据文档的来源，而不是单个工具本身。运行时会先加载 source，再去
 * 拉取或解析 source 指向的元数据内容。
 *
 * @param sourceId 元数据源唯一标识
 * @param name 元数据源展示名
 * @param metaUrl 元数据地址，可为 HTTP(S) 地址或 `classpath:` / `file:` 地址
 * @param inlineContent 直接存储的元数据内容，适合上传文件场景
 * @param enabled 是否启用
 * @param headers 拉取元数据时附带的请求头
 * @param timeout 拉取元数据时的超时时间
 * @param refreshInterval 元数据建议刷新周期
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record RemoteToolSourceDefinition(
        String sourceId,
        String name,
        String metaUrl,
        String inlineContent,
        boolean enabled,
        Map<String, String> headers,
        Duration timeout,
        Duration refreshInterval,
        Instant createdAt,
        Instant updatedAt) {
}
