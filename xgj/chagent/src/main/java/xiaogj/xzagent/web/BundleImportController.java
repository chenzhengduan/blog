package xiaogj.xzagent.web;

import xiaogj.xzagent.service.BundleImportService;
import xiaogj.xzagent.web.dto.BundleImportResponse;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Bundle 导入接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/bundles")
public class BundleImportController {

    private final BundleImportService bundleImportService;

    public BundleImportController(BundleImportService bundleImportService) {
        this.bundleImportService = bundleImportService;
    }

    /**
     * 上传并导入 bundle 压缩包。
     */
    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<BundleImportResponse> importBundle(
            @RequestParam(value = "force", defaultValue = "false") boolean force,
            @RequestPart("file") FilePart file) {
        return bundleImportService.importBundle(file, force);
    }
}
