package xiaogj.xzagent.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.net.InetAddress;
import java.util.Properties;

/**
 * Nacos 服务注册与注销组件。
 *
 * <p>使用原生 nacos-client 1.4.x（HTTP 协议），兼容 Nacos 1.3.x/1.4.x 服务端。
 * 应用启动完成后向 Nacos 注册实例，应用关闭时自动注销，保证服务列表的实时性。
 *
 * <p>可通过 {@code nacos.register-enabled=false} 禁用注册，适用于本地开发场景。
 */
@Configuration
@ConditionalOnProperty(name = "nacos.register-enabled", havingValue = "true", matchIfMissing = true)
public class NacosRegistrationConfig {

    private static final Logger log = LoggerFactory.getLogger(NacosRegistrationConfig.class);

    /** Nacos 服务器地址，格式 host:port */
    @Value("${nacos.server-addr}")
    private String serverAddr;

    /** 命名空间 ID，为空则使用 public 空间 */
    @Value("${nacos.namespace:}")
    private String namespace;

    /** 服务分组 */
    @Value("${nacos.group:DEFAULT_GROUP}")
    private String group;

    /** 注册的服务名，取 spring.application.name */
    @Value("${spring.application.name}")
    private String serviceName;

    /** 应用监听端口，用于实例注册 */
    @Value("${server.port:8080}")
    private int port;

    /** NamingService 实例，注销时复用 */
    private NamingService namingService;

    /** 已注册的实例 IP */
    private String instanceIp;

    /**
     * 应用启动完成后向 Nacos 注册服务实例。
     * 使用 ApplicationReadyEvent 确保所有 Bean 初始化完毕后再注册，
     * 避免服务已可被调用但内部组件尚未就绪的情况。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        try {
            Properties props = buildNacosProperties();
            namingService = NacosFactory.createNamingService(props);
            instanceIp = InetAddress.getLocalHost().getHostAddress();

            Instance instance = new Instance();
            instance.setIp(instanceIp);
            instance.setPort(port);
            instance.setServiceName(serviceName);
            instance.setHealthy(true);
            instance.setEnabled(true);

            namingService.registerInstance(serviceName, group, instance);
            log.info("[Nacos] 服务注册成功 => {}:{} 注册到 {} group={} namespace={}",
                    instanceIp, port, serverAddr, group, namespace);
        } catch (Exception e) {
            // 注册失败不影响应用正常启动，仅记录错误
            log.error("[Nacos] 服务注册失败，应用将以独立模式运行: {}", e.getMessage(), e);
        }
    }

    /**
     * 应用上下文关闭时从 Nacos 注销服务实例。
     * 及时注销可避免其他服务调用到已停止的实例（等待心跳超时约需 15 秒才会自动摘除）。
     */
    @EventListener(ContextClosedEvent.class)
    public void deregister() {
        if (namingService == null || instanceIp == null) {
            return;
        }
        try {
            namingService.deregisterInstance(serviceName, group, instanceIp, port);
            log.info("[Nacos] 服务注销成功 => {}:{}", instanceIp, port);
        } catch (NacosException e) {
            log.warn("[Nacos] 服务注销失败: {}", e.getMessage());
        }
    }

    /**
     * 构建 Nacos 客户端连接属性。
     *
     * @return Properties 包含 serverAddr 和 namespace
     */
    private Properties buildNacosProperties() {
        Properties props = new Properties();
        props.put("serverAddr", serverAddr);
        if (namespace != null && !namespace.isBlank()) {
            props.put("namespace", namespace);
        }
        return props;
    }
}
