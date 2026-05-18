package xiaogj.xzagent.web;

import java.util.List;
import xiaogj.xzagent.service.LocalToolCatalogService;
import xiaogj.xzagent.web.dto.LocalToolResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本地工具目录接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/local-tools")
public class LocalToolCatalogController {

    private final LocalToolCatalogService localToolCatalogService;

    public LocalToolCatalogController(LocalToolCatalogService localToolCatalogService) {
        this.localToolCatalogService = localToolCatalogService;
    }

    /**
     * 列出当前后端注册的本地工具。
     */
    @GetMapping
    public List<LocalToolResponse> listTools() {
        return localToolCatalogService.listTools();
    }
}
