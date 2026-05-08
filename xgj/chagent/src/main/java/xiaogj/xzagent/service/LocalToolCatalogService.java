package xiaogj.xzagent.service;

import io.agentscope.core.tool.AgentTool;
import java.util.Comparator;
import java.util.List;
import xiaogj.xzagent.web.dto.LocalToolResponse;
import org.springframework.stereotype.Service;

/**
 * 本地工具目录服务。
 *
 * <p>该服务把当前后端注册的内置本地工具整理成只读目录，供设置页做 Skill 绑定时
 * 展示候选项。这样前端不需要猜当前有哪些本地工具。
 */
@Service
public class LocalToolCatalogService {

    private final List<AgentTool> builtInAgentTools;

    public LocalToolCatalogService(List<AgentTool> builtInAgentTools) {
        this.builtInAgentTools = builtInAgentTools;
    }

    /**
     * 列出全部本地工具。
     */
    public List<LocalToolResponse> listTools() {
        return builtInAgentTools.stream()
                .map(tool -> new LocalToolResponse(tool.getName(), tool.getDescription()))
                .sorted(Comparator.comparing(LocalToolResponse::name))
                .toList();
    }
}
