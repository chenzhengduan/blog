package xiaogj.xzagent.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import xiaogj.xzagent.config.XzagentDistributedRunProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 当前节点身份提供器。
 *
 * <p>分布式运行协调层需要一个稳定的 {@code nodeId} 来标识 owner 节点，
 * 同时也需要一个可被集群内其他节点访问的内部基地址用于控制转发。
 * 这两个值都允许显式配置；未配置时再做尽量稳定的本机推导。
 */
@Component
public class NodeIdentityProvider {

    /** 分布式运行配置。 */
    private final XzagentDistributedRunProperties properties;
    /** 当前服务名，用于推导默认 nodeId。 */
    private final String serviceName;
    /** 当前服务监听端口，用于推导默认 nodeId / baseUrl。 */
    private final int serverPort;
    /** 缓存后的节点标识，避免重复做主机解析。 */
    private final String currentNodeId;
    /** 缓存后的内部基地址。 */
    private final String internalBaseUrl;

    public NodeIdentityProvider(
            XzagentDistributedRunProperties properties,
            Environment environment,
            @Value("${server.port:8080}") int serverPort) {
        this.properties = properties;
        this.serviceName = environment.getProperty("spring.application.name", "application");
        this.serverPort = serverPort;
        this.currentNodeId = resolveNodeId();
        this.internalBaseUrl = resolveInternalBaseUrl();
    }

    /**
     * 返回当前节点稳定标识。
     */
    public String currentNodeId() {
        return currentNodeId;
    }

    /**
     * 返回当前节点供集群内部调用的基地址。
     */
    public String internalBaseUrl() {
        return internalBaseUrl;
    }

    /**
     * 返回内部控制路径前缀。
     */
    public String internalControlPathPrefix() {
        return properties.normalizedInternalControlPathPrefix();
    }

    private String resolveNodeId() {
        if (properties.nodeId() != null && !properties.nodeId().isBlank()) {
            return properties.nodeId().trim();
        }
        String hostAddress = resolveHostAddress();
        return serviceName + ":" + hostAddress + ":" + serverPort;
    }

    private String resolveInternalBaseUrl() {
        if (properties.internalBaseUrl() != null && !properties.internalBaseUrl().isBlank()) {
            return trimTrailingSlash(properties.internalBaseUrl().trim());
        }
        return "http://" + resolveHostAddress() + ":" + serverPort;
    }

    private String resolveHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException exception) {
            return "127.0.0.1";
        }
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
