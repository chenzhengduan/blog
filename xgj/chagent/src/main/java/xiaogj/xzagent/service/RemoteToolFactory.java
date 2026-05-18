package xiaogj.xzagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.AgentTool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.config.XzagentRemoteToolLoggingProperties;
import xiaogj.xzagent.model.remote.RemoteToolDefinition;
import xiaogj.xzagent.model.remote.RemoteToolMetaDocument;
import xiaogj.xzagent.tool.RemoteHttpAgentTool;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 远程工具工厂。
 */
@Service
public class RemoteToolFactory {

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    private final UserRemoteSourceCredentialBindingService bindingService;
    private final XzagentRemoteToolLoggingProperties remoteToolLoggingProperties;

    public RemoteToolFactory(
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            UserRemoteSourceCredentialBindingService bindingService,
            XzagentRemoteToolLoggingProperties remoteToolLoggingProperties) {
        this.objectMapper = objectMapper;
        this.webClientBuilder = webClientBuilder;
        this.bindingService = bindingService;
        this.remoteToolLoggingProperties = remoteToolLoggingProperties;
    }

    /**
     * 基于运行时快照实例化启用状态的远程 AgentTool。
     */
    public List<AgentTool> createEnabledTools(RuntimeSnapshot snapshot, Long userId, String username) {
        List<AgentTool> tools = new ArrayList<>();
        for (Map.Entry<String, RemoteToolMetaDocument> entry : snapshot.remoteMetaDocuments().entrySet()) {
            String sourceId = entry.getKey();
            RemoteToolMetaDocument document = entry.getValue();
            if (document.tools() == null || document.tools().isEmpty()) {
                continue;
            }
            for (RemoteToolDefinition definition : document.tools()) {
                if (definition == null || definition.name() == null || definition.name().isBlank()) {
                    continue;
                }
                if (Boolean.FALSE.equals(definition.enabled())) {
                    continue;
                }
                tools.add(new RemoteHttpAgentTool(
                        sourceId,
                        userId,
                        username,
                        document,
                        definition,
                        objectMapper,
                        webClientBuilder.build(),
                        bindingService,
                        remoteToolLoggingProperties));
            }
        }
        return tools;
    }
}
