package xiaogj.xzagent.model.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * 远程工具元数据标准的顶层文档对象。
 *
 * <p>该对象对应 `docs/backend/remote-tool-meta-standard.md` 中定义的
 * 运行时标准。解析阶段只关注结构完整性，不直接承担业务校验职责。
 * 后续若要注册为 Tool 或 MCP Tool，应在更高一层服务中追加合法性校验。
 *
 * @param version 文档版本
 * @param provider Provider 信息
 * @param defaults 默认配置
 * @param tools 工具列表
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteToolMetaDocument(
        String version,
        RemoteToolProvider provider,
        RemoteToolDefaults defaults,
        List<RemoteToolDefinition> tools) {
}

