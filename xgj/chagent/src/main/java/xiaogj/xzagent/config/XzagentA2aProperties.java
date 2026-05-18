package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Xzagent A2A 暴露配置。
 *
 * <p>当前项目只把“默认 agent”暴露成一个 A2A 服务，因此配置保持最小集合：
 * 开关、对外地址以及 AgentCard 的基本描述。后续如果扩展为多 agent，对应的
 * server/card/path 也可以在这个配置模型基础上继续拆分。
 *
 * @param enabled 是否启用 A2A 暴露
 * @param host A2A AgentCard 中声明的主机地址
 * @param port A2A AgentCard 中声明的端口
 * @param cardName AgentCard 对外展示名称
 * @param cardDescription AgentCard 对外展示描述
 * @param version AgentCard 版本号
 */
@ConfigurationProperties(prefix = "xzagent.a2a")
public record XzagentA2aProperties(
        boolean enabled,
        String host,
        int port,
        String cardName,
        String cardDescription,
        String version) {
}
