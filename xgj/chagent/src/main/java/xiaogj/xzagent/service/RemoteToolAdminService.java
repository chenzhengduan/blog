package xiaogj.xzagent.service;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import xiaogj.xzagent.repository.SkillRemoteToolBindingRepository;
import xiaogj.xzagent.repository.UserRemoteSourceCredentialBindingRepository;
import xiaogj.xzagent.web.dto.InlineRemoteToolSourceRequest;
import xiaogj.xzagent.web.dto.RemoteToolSourceRequest;
import xiaogj.xzagent.web.dto.RemoteToolSourceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.core.io.buffer.DataBufferUtils;

/**
 * 远程工具元数据源管理服务。
 */
@Service
public class RemoteToolAdminService {

    private static final Logger log = LoggerFactory.getLogger(RemoteToolAdminService.class);

    private final RemoteToolSourceRepository remoteToolSourceRepository;
    private final RuntimeSnapshotRegistry runtimeSnapshotRegistry;
    private final SkillRemoteToolBindingRepository skillRemoteToolBindingRepository;
    private final UserRemoteSourceCredentialBindingRepository userRemoteSourceCredentialBindingRepository;

    public RemoteToolAdminService(
            RemoteToolSourceRepository remoteToolSourceRepository,
            RuntimeSnapshotRegistry runtimeSnapshotRegistry,
            SkillRemoteToolBindingRepository skillRemoteToolBindingRepository,
            UserRemoteSourceCredentialBindingRepository userRemoteSourceCredentialBindingRepository) {
        this.remoteToolSourceRepository = remoteToolSourceRepository;
        this.runtimeSnapshotRegistry = runtimeSnapshotRegistry;
        this.skillRemoteToolBindingRepository = skillRemoteToolBindingRepository;
        this.userRemoteSourceCredentialBindingRepository =
                userRemoteSourceCredentialBindingRepository;
    }

    /**
     * 列出全部远程工具源配置。
     */
    public List<RemoteToolSourceResponse> listSources() {
        return remoteToolSourceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 保存基于 URL 的远程工具元数据源。
     *
     * <p>这里仅更新远程来源自身配置，不再根据元数据文档重写已有 Skill 绑定。
     * Skill 绑定应继续以平台数据库中的绑定配置为准，避免“编辑来源”意外覆盖
     * “绑定关系”。
     */
    public RemoteToolSourceResponse saveUrlSource(RemoteToolSourceRequest request) {
        Instant now = Instant.now();
        RemoteToolSourceDefinition definition = new RemoteToolSourceDefinition(
                request.sourceId(),
                request.name(),
                request.metaUrl(),
                null,
                request.enabled(),
                request.headers(),
                Duration.ofSeconds(request.timeoutSeconds()),
                Duration.ofSeconds(request.refreshIntervalSeconds()),
                now,
                now);
        log.info("保存 URL 远程来源: sourceId={}, name={}", definition.sourceId(), definition.name());
        remoteToolSourceRepository.upsert(definition);
        runtimeSnapshotRegistry.refresh();
        return toResponse(definition);
    }

    /**
     * 保存基于内联 JSON 文本的远程工具元数据源。
     *
     * <p>该入口面向 Web 前端：浏览器先读取文件文本，再通过普通 JSON 请求提交。
     * 后端只负责落库和解析，不再参与 multipart 上传细节。
     *
     * <p>注意：保存来源内容不会再自动改写原有 Skill 绑定，避免管理员只是更新
     * JSON 内容时，之前在平台里手工维护的绑定关系被覆盖。
     */
    public RemoteToolSourceResponse saveInlineSource(InlineRemoteToolSourceRequest request) {
        validateRequiredText(request.sourceId(), "sourceId");
        validateRequiredText(request.name(), "name");
        validateRequiredText(request.inlineContent(), "inlineContent");
        Instant now = Instant.now();
        RemoteToolSourceDefinition definition = new RemoteToolSourceDefinition(
                request.sourceId(),
                request.name(),
                null,
                request.inlineContent(),
                request.enabled(),
                Map.of(),
                Duration.ofSeconds(request.timeoutSeconds()),
                Duration.ofSeconds(request.refreshIntervalSeconds()),
                now,
                now);
        log.info(
                "保存内联远程来源: sourceId={}, name={}, contentLength={}",
                definition.sourceId(),
                definition.name(),
                definition.inlineContent() != null ? definition.inlineContent().length() : 0);
        remoteToolSourceRepository.upsert(definition);
        runtimeSnapshotRegistry.refresh();
        return toResponse(definition);
    }

    /**
     * 保存上传文件形式的远程工具元数据源。
     *
     * <p>这里和内联保存保持一致：上传新文件仅覆盖远程来源定义本身，不触碰已有
     * Skill 绑定。这样“重新上传 JSON 文件”就不会再导致管理员需要重新绑定。
     */
    public RemoteToolSourceResponse saveUploadedSource(
            String sourceId,
            String name,
            boolean enabled,
            Integer timeoutSeconds,
            Integer refreshIntervalSeconds,
            FilePart filePart) {
        validateRequiredText(sourceId, "sourceId");
        validateRequiredText(name, "name");
        String content = DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        return new String(bytes, StandardCharsets.UTF_8);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .block();
        log.info(
                "保存上传远程来源: sourceId={}, name={}, fileName={}, contentLength={}",
                sourceId,
                name,
                filePart.filename(),
                content != null ? content.length() : 0);
        validateRequiredText(content, "inlineContent");
        Instant now = Instant.now();
        RemoteToolSourceDefinition definition = new RemoteToolSourceDefinition(
                sourceId,
                name,
                null,
                content,
                enabled,
                Map.of(),
                Duration.ofSeconds(timeoutSeconds != null ? timeoutSeconds : 10),
                Duration.ofSeconds(refreshIntervalSeconds != null ? refreshIntervalSeconds : 300),
                now,
                now);
        remoteToolSourceRepository.upsert(definition);
        runtimeSnapshotRegistry.refresh();
        return toResponse(definition);
    }

    /**
     * 删除远程来源，并同时清理该来源下的 Skill 绑定。
     *
     * <p>这里显式把“删除来源”和“清理绑定”绑定在同一事务语义里，避免数据库里
     * 残留指向不存在来源的悬空绑定记录。编辑来源不会动绑定，但删除来源必须清理。
     *
     * @param sourceId 来源唯一标识
     */
    public void deleteSource(String sourceId) {
        validateRequiredText(sourceId, "sourceId");
        log.info("删除远程来源并清理绑定: sourceId={}", sourceId);
        skillRemoteToolBindingRepository.deleteBySourceId(sourceId);
        userRemoteSourceCredentialBindingRepository.deleteBySourceId(sourceId);
        remoteToolSourceRepository.deleteBySourceId(sourceId);
        runtimeSnapshotRegistry.refresh();
    }

    private RemoteToolSourceResponse toResponse(RemoteToolSourceDefinition definition) {
        return new RemoteToolSourceResponse(
                definition.sourceId(),
                definition.name(),
                definition.metaUrl(),
                definition.inlineContent() != null && !definition.inlineContent().isBlank(),
                definition.enabled(),
                definition.timeout().toSeconds(),
                definition.refreshInterval().toSeconds(),
                definition.updatedAt());
    }

    /**
     * 统一校验必填文本字段，避免空字符串一路传到更深层才失败。
     */
    private void validateRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
    }
}
