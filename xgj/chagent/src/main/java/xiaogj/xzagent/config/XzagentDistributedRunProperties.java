package xiaogj.xzagent.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式运行协调配置。
 *
 * <p>该配置只描述“运行中协调层”，不负责会话历史、A2UI Surface 或
 * AgentScope Session 的持久化。也就是说，这里的参数主要影响：
 * <ul>
 *     <li>活跃 run 的 lease / heartbeat 行为</li>
 *     <li>Redis Stream 事件保留时长</li>
 *     <li>节点间内部控制转发地址</li>
 * </ul>
 *
 * @param nodeId 当前节点稳定标识；为空时自动推导
 * @param internalBaseUrl 当前节点供集群内部访问的基地址；为空时自动推导
 * @param activeLeaseSeconds 活跃 run 的 lease 时长，单位秒
 * @param heartbeatIntervalSeconds heartbeat 刷新间隔，单位秒
 * @param eventStreamTtlSeconds Redis Stream 保留时长，单位秒
 * @param replayBatchSize 单次补流最多返回的事件条数
 * @param livePollTimeoutMillis live 轮询 Redis Stream 的阻塞时长，单位毫秒
 * @param internalControlPathPrefix 内部控制端点路径前缀
 */
@ConfigurationProperties(prefix = "xzagent.distributed-run")
public record XzagentDistributedRunProperties(
        String nodeId,
        String internalBaseUrl,
        long activeLeaseSeconds,
        long heartbeatIntervalSeconds,
        long eventStreamTtlSeconds,
        int replayBatchSize,
        long livePollTimeoutMillis,
        String internalControlPathPrefix) {

    /**
     * 活跃运行 lease 时长。
     */
    public Duration activeLeaseDuration() {
        return Duration.ofSeconds(Math.max(activeLeaseSeconds, 1L));
    }

    /**
     * heartbeat 间隔。
     */
    public Duration heartbeatIntervalDuration() {
        return Duration.ofSeconds(Math.max(heartbeatIntervalSeconds, 1L));
    }

    /**
     * 事件流保留时长。
     */
    public Duration eventStreamTtlDuration() {
        return Duration.ofSeconds(Math.max(eventStreamTtlSeconds, 1L));
    }

    /**
     * live 轮询阻塞时长。
     */
    public Duration livePollTimeoutDuration() {
        return Duration.ofMillis(Math.max(livePollTimeoutMillis, 1L));
    }

    /**
     * 统一规范内部控制路径前缀，避免前后多余斜杠带来拼接歧义。
     */
    public String normalizedInternalControlPathPrefix() {
        if (internalControlPathPrefix == null || internalControlPathPrefix.isBlank()) {
            return "/xzagent/private/internal";
        }
        String normalized = internalControlPathPrefix.startsWith("/")
                ? internalControlPathPrefix
                : "/" + internalControlPathPrefix;
        return normalized.endsWith("/") && normalized.length() > 1
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }
}
