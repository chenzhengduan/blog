package xiaogj.xzagent.web.dto;

/**
 * Bundle 导入问题项响应。
 *
 * @param stage 失败阶段
 * @param resourceType 资源类型
 * @param resourceId 资源标识
 * @param message 错误说明
 * @param fatal 是否为致命问题
 */
public record BundleImportIssueResponse(
        String stage,
        String resourceType,
        String resourceId,
        String message,
        boolean fatal) {
}
