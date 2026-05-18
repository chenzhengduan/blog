package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import xiaogj.xzagent.service.AgentDefinitionService;
import xiaogj.xzagent.web.dto.AgentDefinitionRequest;
import xiaogj.xzagent.web.dto.AgentDefinitionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 管理接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/agent")
public class AgentAdminController {

    private final AgentDefinitionService agentDefinitionService;

    public AgentAdminController(AgentDefinitionService agentDefinitionService) {
        this.agentDefinitionService = agentDefinitionService;
    }

    /**
     * 获取当前唯一 Agent 配置。
     */
    @GetMapping
    public AgentDefinitionResponse getCurrentAgent() {
        return agentDefinitionService.getCurrentDefinition();
    }

    /**
     * 保存当前唯一 Agent 配置。
     */
    @PutMapping
    public AgentDefinitionResponse saveAgent(@Valid @RequestBody AgentDefinitionRequest request) {
        return agentDefinitionService.saveDefinition(request);
    }
}

