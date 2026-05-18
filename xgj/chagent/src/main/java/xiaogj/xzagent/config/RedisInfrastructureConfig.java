package xiaogj.xzagent.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 基础设施配置。
 *
 * <p>这里不依赖 Spring Boot 对 {@code spring.data.redis.*} 的自动装配，
 * 而是显式根据项目已有的 {@code spring.redis.*} 配置创建连接工厂。
 * 这样做可以在不迁移 Nacos / 环境变量键名的前提下，直接接入
 * 分布式运行协调与 Redis Stream 存储。
 */
@Configuration
public class RedisInfrastructureConfig {

    /**
     * 创建 Redis 连接工厂。
     *
     * <p>当前仅支持单机 / 主节点地址配置，已经满足现有环境的接入需求。
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(SpringRedisProperties properties) {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(properties.host(), properties.port());
        configuration.setDatabase(properties.database());
        if (properties.hasPassword()) {
            configuration.setPassword(RedisPassword.of(properties.password()));
        }

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(properties.timeoutDuration())
                .shutdownTimeout(Duration.ZERO)
                .build();
        return new LettuceConnectionFactory(configuration, clientConfiguration);
    }

    /**
     * 创建字符串模板。
     *
     * <p>分布式运行协调层当前只操作字符串 key、hash、stream 字段，
     * 使用 {@link StringRedisTemplate} 最直接，也最便于排障。
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
