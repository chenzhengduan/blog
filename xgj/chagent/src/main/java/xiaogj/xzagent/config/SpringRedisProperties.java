package xiaogj.xzagent.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 连接配置。
 *
 * <p>当前项目历史上一直使用 {@code spring.redis.*} 这一组配置键，
 * 而不是 Spring Boot 3 默认推荐的 {@code spring.data.redis.*}。
 * 为了避免改动现网配置中心，这里显式定义一份属性模型，并在
 * {@link RedisInfrastructureConfig} 中手动创建连接工厂。
 *
 * @param database Redis 数据库索引
 * @param host Redis 主机地址
 * @param port Redis 端口
 * @param password Redis 密码，可为空
 * @param timeout 命令超时时间，单位毫秒
 */
@ConfigurationProperties(prefix = "spring.redis")
public record SpringRedisProperties(
        int database,
        String host,
        int port,
        String password,
        long timeout) {

    /**
     * 是否显式配置了密码。
     */
    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    /**
     * 将毫秒超时转换为 Duration，便于 Lettuce 连接工厂直接使用。
     */
    public Duration timeoutDuration() {
        return Duration.ofMillis(Math.max(timeout, 1L));
    }
}
