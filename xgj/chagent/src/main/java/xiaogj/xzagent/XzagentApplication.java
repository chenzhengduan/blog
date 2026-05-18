package xiaogj.xzagent;

import xiaogj.xzagent.config.XzagentAgentProperties;
import xiaogj.xzagent.config.XzagentA2aProperties;
import xiaogj.xzagent.config.XzagentAnthropicProperties;
import xiaogj.xzagent.config.XzagentAutoContextProperties;
import xiaogj.xzagent.config.SpringRedisProperties;
import xiaogj.xzagent.config.XzagentDashScopeProperties;
import xiaogj.xzagent.config.XzagentDistributedRunProperties;
import xiaogj.xzagent.config.XzagentHtmlScreenshotProperties;
import xiaogj.xzagent.config.XzagentImageGenerateProperties;
import xiaogj.xzagent.config.XzagentModelProperties;
import xiaogj.xzagent.config.XzagentOpenAiProperties;
import xiaogj.xzagent.config.XzagentRemoteToolLoggingProperties;
import xiaogj.xzagent.config.XzagentSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableConfigurationProperties({
    XzagentModelProperties.class,
    XzagentAnthropicProperties.class,
    XzagentOpenAiProperties.class,
    XzagentAutoContextProperties.class,
    XzagentAgentProperties.class,
    XzagentA2aProperties.class,
    XzagentSecurityProperties.class,
    XzagentRemoteToolLoggingProperties.class,
    XzagentHtmlScreenshotProperties.class,
    XzagentDashScopeProperties.class,
    XzagentImageGenerateProperties.class,
    XzagentDistributedRunProperties.class,
    SpringRedisProperties.class
})
public class XzagentApplication {

    /**
     * Xzagent 后端启动入口。
     *
     * <p>当前应用承担三类职责：
     * <ul>
     *     <li>暴露单 Agent 聊天接口和会话接口。</li>
     *     <li>装配 AgentScope 运行时和技能、工具、MCP 客户端。</li>
     *     <li>同时托管 Flutter Web 构建产物作为同端口静态资源。</li>
     * </ul>
     *
     * <p>启动前预设本机 IP 系统属性 {@code app.local.ip}，供 logback-spring.xml 中
     * 阿里云 SLS Appender 的 source 字段使用。使用 2s 超时防止 DNS 异常阻塞启动。
     */
    public static void main(String[] args) {
        // 在 Spring 启动前将本机 IP 写入系统属性，logback 初始化时即可通过 ${app.local.ip} 读取
        try {
            String ip = CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            return InetAddress.getLocalHost().getHostAddress();
                        } catch (Exception e) {
                            return "unknown";
                        }
                    })
                    .get(2, TimeUnit.SECONDS);
            System.setProperty("app.local.ip", ip);
        } catch (Exception e) {
            System.setProperty("app.local.ip", "unknown");
        }

        SpringApplication.run(XzagentApplication.class, args);
    }
}
