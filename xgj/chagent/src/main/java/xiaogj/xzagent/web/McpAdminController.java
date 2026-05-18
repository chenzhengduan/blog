package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import java.util.List;
import xiaogj.xzagent.service.McpAdminService;
import xiaogj.xzagent.web.dto.McpServerRequest;
import xiaogj.xzagent.web.dto.McpServerResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCP 配置管理接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/mcp-servers")
public class McpAdminController {

    private final McpAdminService mcpAdminService;

    public McpAdminController(McpAdminService mcpAdminService) {
        this.mcpAdminService = mcpAdminService;
    }

    /**
     * 列出全部 MCP Server 配置。
     */
    @GetMapping
    public List<McpServerResponse> listServers() {
        return mcpAdminService.listServers();
    }

    /**
     * 保存或更新 MCP Server 配置。
     */
    @PostMapping
    public McpServerResponse saveServer(@Valid @RequestBody McpServerRequest request) {
        return mcpAdminService.saveServer(request);
    }
}

