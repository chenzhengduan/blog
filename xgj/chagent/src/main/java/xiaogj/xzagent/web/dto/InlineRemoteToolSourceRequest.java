package xiaogj.xzagent.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 基于内联 JSON 文本的远程工具源保存请求。
 *
 * <p>前端在浏览器本地读取并解析文件文本，再通过普通 JSON 请求提交到后端。
 * 这样可以绕开 WebFlux `multipart/form-data` 在不同浏览器与运行环境下的
 * 兼容问题，同时让接口更容易调试和复现。
 */
public record InlineRemoteToolSourceRequest(
        @NotBlank String sourceId,
        @NotBlank String name,
        @NotBlank String inlineContent,
        @NotNull Boolean enabled,
        @Min(1) long timeoutSeconds,
        @Min(1) long refreshIntervalSeconds) {
}
