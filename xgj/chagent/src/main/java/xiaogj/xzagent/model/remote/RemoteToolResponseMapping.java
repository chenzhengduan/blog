package xiaogj.xzagent.model.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 远程工具响应提取配置。
 *
 * @param type 映射类型，例如 `raw`、`json_path`
 * @param path JSON Path 表达式
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteToolResponseMapping(
        String type,
        String path) {
}

