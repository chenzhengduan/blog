package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import java.util.List;
import xiaogj.xzagent.service.RemoteToolAdminService;
import xiaogj.xzagent.web.dto.InlineRemoteToolSourceRequest;
import xiaogj.xzagent.web.dto.RemoteToolSourceRequest;
import xiaogj.xzagent.web.dto.RemoteToolSourceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 远程工具元数据源管理接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/remote-tool-sources")
public class RemoteToolAdminController {

    private static final Logger log = LoggerFactory.getLogger(RemoteToolAdminController.class);

    private final RemoteToolAdminService remoteToolAdminService;

    public RemoteToolAdminController(RemoteToolAdminService remoteToolAdminService) {
        this.remoteToolAdminService = remoteToolAdminService;
    }

    /**
     * 列出全部远程工具源。
     */
    @GetMapping
    public List<RemoteToolSourceResponse> listSources() {
        return remoteToolAdminService.listSources();
    }

    /**
     * 通过远程 URL 保存元数据源。
     */
    @PostMapping
    public RemoteToolSourceResponse saveSource(@Valid @RequestBody RemoteToolSourceRequest request) {
        log.info(
                "收到远程来源地址配置: sourceId={}, name={}, metaUrl={}, timeoutSeconds={}, refreshIntervalSeconds={}",
                request.sourceId(),
                request.name(),
                request.metaUrl(),
                request.timeoutSeconds(),
                request.refreshIntervalSeconds());
        return remoteToolAdminService.saveUrlSource(request);
    }

    /**
     * 通过内联 JSON 文本保存元数据源。
     */
    @PostMapping(path = "/inline", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RemoteToolSourceResponse saveInlineSource(
            @Valid @RequestBody InlineRemoteToolSourceRequest request) {
        log.info(
                "收到内联远程来源配置: sourceId={}, name={}, contentLength={}, timeoutSeconds={}, refreshIntervalSeconds={}",
                request.sourceId(),
                request.name(),
                request.inlineContent() != null ? request.inlineContent().length() : 0,
                request.timeoutSeconds(),
                request.refreshIntervalSeconds());
        return remoteToolAdminService.saveInlineSource(request);
    }

    /**
     * 通过上传 JSON 文件保存元数据源。
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<RemoteToolSourceResponse> uploadSource(
            @RequestPart("sourceId") FormFieldPart sourceId,
            @RequestPart("name") FormFieldPart name,
            @RequestPart("enabled") FormFieldPart enabled,
            @RequestPart(value = "timeoutSeconds", required = false) FormFieldPart timeoutSeconds,
            @RequestPart(value = "refreshIntervalSeconds", required = false) FormFieldPart refreshIntervalSeconds,
            @RequestPart("file") FilePart file) {
        log.info(
                "收到 multipart 远程来源上传: sourceId={}, name={}, fileName={}, timeoutSeconds={}, refreshIntervalSeconds={}",
                sourceId.value(),
                name.value(),
                file.filename(),
                timeoutSeconds != null ? timeoutSeconds.value() : null,
                refreshIntervalSeconds != null ? refreshIntervalSeconds.value() : null);
        return Mono.fromCallable(() -> remoteToolAdminService.saveUploadedSource(
                        sourceId.value(),
                        name.value(),
                        enabled != null && Boolean.parseBoolean(enabled.value()),
                        timeoutSeconds != null ? parseNullableInt(timeoutSeconds.value()) : null,
                        refreshIntervalSeconds != null ? parseNullableInt(refreshIntervalSeconds.value()) : null,
                        file));
    }

    /**
     * 解析可选整数表单字段。
     */
    private Integer parseNullableInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    /**
     * 删除远程来源。
     */
    @DeleteMapping("/{sourceId}")
    public void deleteSource(@PathVariable String sourceId) {
        log.info("收到删除远程来源请求: sourceId={}", sourceId);
        remoteToolAdminService.deleteSource(sourceId);
    }
}
