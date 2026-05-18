package xiaogj.xzagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;
import xiaogj.xzagent.model.remote.RemoteToolMetaDocument;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 远程工具元数据加载服务。
 *
 * <p>该服务的职责仅限于“读取并解析元数据标准文档”。它当前不直接注册
 * Tool 或 MCP Tool，而是为下一步的运行时装配提供统一入口。这样可以
 * 先把标准、存储和解析链路打通，再逐步接入具体的 Tool 生成逻辑。
 */
@Service
public class RemoteToolMetaService {

    private static final Logger log = LoggerFactory.getLogger(RemoteToolMetaService.class);

    private final RemoteToolSourceRepository remoteToolSourceRepository;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    public RemoteToolMetaService(
            RemoteToolSourceRepository remoteToolSourceRepository,
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder) {
        this.remoteToolSourceRepository = remoteToolSourceRepository;
        this.objectMapper = objectMapper;
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * 加载所有启用中的远程元数据源，并返回已成功解析的结果。
     *
     * <p>单个元数据源加载失败不会阻断整体流程。这样后续在应用启动或定时
     * 刷新时，局部坏数据不会拖垮整套 Agent 运行时。
     *
     * @return 以 sourceId 为键的已解析元数据文档
     */
    public Map<String, RemoteToolMetaDocument> loadEnabledMetaDocuments() {
        return loadMetaDocuments(remoteToolSourceRepository.findEnabledSources());
    }

    public Map<String, RemoteToolMetaDocument> loadMetaDocuments(List<RemoteToolSourceDefinition> sources) {
        Map<String, RemoteToolMetaDocument> documents = new LinkedHashMap<>();
        for (RemoteToolSourceDefinition source : sources) {
            try {
                documents.put(source.sourceId(), loadMetaDocument(source));
            } catch (RuntimeException e) {
                log.warn("加载远程工具元数据失败: sourceId={}, metaUrl={}, message={}",
                        source.sourceId(), source.metaUrl(), e.getMessage());
            }
        }
        return documents;
    }

    /**
     * 按 source 定位元数据源，并加载其内容。
     */
    public RemoteToolMetaDocument loadMetaDocument(RemoteToolSourceDefinition source) {
        if (source.inlineContent() != null && !source.inlineContent().isBlank()) {
            return loadFromInline(source.inlineContent());
        }
        String metaUrl = source.metaUrl();
        if (metaUrl == null || metaUrl.isBlank()) {
            throw new IllegalArgumentException("远程工具元数据地址不能为空");
        }

        if (metaUrl.startsWith("classpath:")) {
            return loadFromClasspath(metaUrl.substring("classpath:".length()));
        }
        if (metaUrl.startsWith("file:")) {
            Resource resource = new org.springframework.core.io.FileSystemResource(metaUrl.substring("file:".length()));
            return loadFromResource(resource);
        }
        if (metaUrl.startsWith("http://") || metaUrl.startsWith("https://")) {
            return loadFromHttp(source);
        }
        throw new IllegalArgumentException("不支持的远程工具元数据地址: " + metaUrl);
    }

    /**
     * 直接解析数据库中保存的元数据内容。
     */
    private RemoteToolMetaDocument loadFromInline(String inlineContent) {
        try {
            return objectMapper.readValue(inlineContent, RemoteToolMetaDocument.class);
        } catch (IOException e) {
            throw new UncheckedIOException("无法解析内联远程工具元数据", e);
        }
    }

    /**
     * 从 classpath 读取元数据，适合本地示例或构建时静态资源。
     */
    private RemoteToolMetaDocument loadFromClasspath(String path) {
        return loadFromResource(new ClassPathResource(path));
    }

    /**
     * 从 Spring Resource 读取并解析 JSON 文档。
     */
    private RemoteToolMetaDocument loadFromResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, RemoteToolMetaDocument.class);
        } catch (IOException e) {
            throw new UncheckedIOException("无法读取远程工具元数据资源", e);
        }
    }

    /**
     * 从 HTTP(S) 地址拉取元数据。
     *
     * <p>这里显式套用 source 级请求头和超时设置，为后续扩展为缓存、
     * 刷新和签名校验留下稳定入口。
     */
    private RemoteToolMetaDocument loadFromHttp(RemoteToolSourceDefinition source) {
        WebClient.Builder builder = webClientBuilder.clone();
        source.headers().forEach((key, value) -> builder.defaultHeader(key, value));
        Duration timeout = source.timeout() != null ? source.timeout() : Duration.ofSeconds(10);

        return builder.build()
                .get()
                .uri(source.metaUrl())
                .retrieve()
                .bodyToMono(RemoteToolMetaDocument.class)
                .timeout(timeout)
                .onErrorMap(throwable ->
                        new IllegalStateException("拉取远程工具元数据失败: " + source.metaUrl(), throwable))
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("远程工具元数据响应为空"));
    }
}
