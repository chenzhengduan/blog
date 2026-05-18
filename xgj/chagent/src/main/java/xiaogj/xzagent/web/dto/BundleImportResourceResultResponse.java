package xiaogj.xzagent.web.dto;

/**
 * Bundle 导入资源级结果。
 *
 * @param stage 导入阶段
 * @param resourceType 资源类型
 * @param resourceId 资源标识
 * @param status 资源状态：APPLIED / SKIPPED / FAILED
 * @param message 结果说明
 */
public record BundleImportResourceResultResponse(
        String stage,
        String resourceType,
        String resourceId,
        String status,
        String message) {
}
