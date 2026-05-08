package xiaogj.xzagent.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ByteArrayResource;

import java.util.Properties;

/**
 * Nacos 配置中心加载器（EnvironmentPostProcessor）。
 *
 * <p>在 Spring 上下文刷新之前，按以下优先级顺序从 Nacos 拉取配置并注入 Environment：
 * <pre>
 *   高 │ xzagent-dev.yml   ← 环境专属配置（由 spring.profiles.active 决定）
 *      │ xzagent.yml       ← 基础公共配置
 *   低 │ application.yml  ← 本地兜底配置
 * </pre>
 *
 * <p>环境区分规则：
 * <ul>
 *   <li>未设置 profile 时只加载 {@code xzagent.yml}</li>
 *   <li>设置 {@code spring.profiles.active=dev} 时额外加载 {@code xzagent-dev.yml}，
 *       其中的配置项会覆盖 {@code xzagent.yml} 中的同名项</li>
 *   <li>支持 dev / test / sit / prd 等任意 profile 名称</li>
 * </ul>
 *
 * <p>任一配置在 Nacos 中不存在时，跳过该层加载（不报错，继续用低优先级配置）。
 * Nacos 整体不可用时，降级使用本地 application.yml，不阻断启动。
 *
 * <p>通过 {@code nacos.config-enabled=false} 可完全关闭配置中心功能。
 */
public class NacosConfigEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigEnvironmentPostProcessor.class);

    /** 拉取配置的超时时间（毫秒） */
    private static final long CONFIG_TIMEOUT_MS = 5000L;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 未启用配置中心则跳过
        if (!Boolean.parseBoolean(environment.getProperty("nacos.config-enabled", "true"))) {
            return;
        }

        String serverAddr = environment.getProperty("nacos.server-addr");
        if (serverAddr == null || serverAddr.isBlank()) {
            log.debug("[Nacos Config] nacos.server-addr 未配置，跳过配置中心加载");
            return;
        }

        String namespace = environment.getProperty("nacos.namespace", "");
        String group     = environment.getProperty("nacos.group", "DEFAULT_GROUP");
        String appName   = environment.getProperty("spring.application.name", "application");
        // 读取激活的 profile，多个时取第一个（主环境标识）
        String profile   = resolveActiveProfile(environment);

        try {
            ConfigService configService = createConfigService(serverAddr, namespace);

            // ① 加载基础公共配置 xzagent.yml（优先级低于环境配置）
            loadConfig(environment, configService, appName + ".yml", group,
                    "nacos-base-config");

            // ② 加载环境专属配置 xzagent-{profile}.yml（存在时优先级最高）
            if (profile != null) {
                loadConfig(environment, configService, appName + "-" + profile + ".yml", group,
                        "nacos-profile-config[" + profile + "]");
            }

        } catch (Exception e) {
            // Nacos 不可用时降级使用本地配置，不阻断启动
            log.error("[Nacos Config] 配置中心连接失败，将使用本地 application.yml: {}", e.getMessage());
        }
    }

    /**
     * 从 Nacos 拉取指定 dataId 的配置，解析为 YAML 后注入 Environment 最高优先级。
     * 若配置不存在或为空则跳过（不视为错误）。
     *
     * @param sourceName PropertySource 在 Environment 中的唯一名称
     */
    private void loadConfig(ConfigurableEnvironment environment,
                            ConfigService configService,
                            String dataId,
                            String group,
                            String sourceName) {
        try {
            String content = configService.getConfig(dataId, group, CONFIG_TIMEOUT_MS);
            if (content == null || content.isBlank()) {
                log.debug("[Nacos Config] 配置不存在，跳过: dataId={} group={}", dataId, group);
                return;
            }

            // YamlPropertySourceLoader 支持嵌套结构和列表，避免手动拍平 YAML
            var sources = new YamlPropertySourceLoader().load(
                    sourceName, new ByteArrayResource(content.getBytes()));

            // addFirst：使 Nacos 配置优先级高于 application.yml；
            // 多次 addFirst 时，后调用的优先级更高（profile 配置最终居顶）
            sources.forEach(source -> environment.getPropertySources().addFirst(source));
            log.info("[Nacos Config] 加载成功: dataId={} group={}", dataId, group);

        } catch (Exception e) {
            log.error("[Nacos Config] 加载失败: dataId={} group={} error={}", dataId, group, e.getMessage());
        }
    }

    /**
     * 解析当前激活的主 profile。
     * 优先读取 {@code spring.profiles.active}，多个 profile 时取第一个。
     *
     * @return profile 名称（如 "dev"），未设置时返回 null
     */
    private String resolveActiveProfile(ConfigurableEnvironment environment) {
        // 先尝试从 spring.profiles.active 属性读取（支持环境变量 SPRING_PROFILES_ACTIVE）
        String activeProfiles = environment.getProperty("spring.profiles.active");
        if (activeProfiles != null && !activeProfiles.isBlank()) {
            // 多 profile 以逗号分隔，取第一个作为主环境标识
            return activeProfiles.split(",")[0].trim();
        }
        // 再尝试从已激活的 profile 列表读取（程序化设置的 profile）
        String[] profiles = environment.getActiveProfiles();
        return profiles.length > 0 ? profiles[0] : null;
    }

    /**
     * 创建 Nacos ConfigService。
     *
     * @param serverAddr Nacos 服务地址，格式 host:port
     * @param namespace  命名空间 ID，为空时使用 public 空间
     */
    private ConfigService createConfigService(String serverAddr, String namespace) throws Exception {
        Properties props = new Properties();
        props.put("serverAddr", serverAddr);
        if (!namespace.isBlank()) {
            props.put("namespace", namespace);
        }
        return NacosFactory.createConfigService(props);
    }

    /**
     * 在 application.yml 加载后、logback 初始化前执行。
     *
     * <p>Spring Boot 启动时序：
     * <ul>
     *   <li>{@code ConfigDataEnvironmentPostProcessor}（HIGHEST+10）加载 application.yml</li>
     *   <li>本处理器（HIGHEST+15）从 Nacos 加载配置，注入 Environment</li>
     *   <li>{@code LoggingApplicationListener}（HIGHEST+20）初始化 logback，读取 {@code <springProperty>}</li>
     * </ul>
     * 调整为 HIGHEST+15 后，logback-spring.xml 中的 {@code <springProperty>} 可读到 Nacos 配置值，
     * 从而支持阿里云 SLS Appender 等依赖 Nacos 配置的日志组件。
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }
}
