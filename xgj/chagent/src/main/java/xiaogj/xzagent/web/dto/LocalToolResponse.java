package xiaogj.xzagent.web.dto;

/**
 * 本地工具目录响应。
 *
 * @param name 工具名
 * @param description 工具说明
 */
public record LocalToolResponse(
        String name,
        String description) {
}
