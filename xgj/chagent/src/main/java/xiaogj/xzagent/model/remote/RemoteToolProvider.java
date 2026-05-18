package xiaogj.xzagent.model.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 远程工具提供方定义。
 *
 * @param name Provider 逻辑名称
 * @param displayName 展示名
 * @param description 描述
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteToolProvider(
        String name,
        String displayName,
        String description) {
}

