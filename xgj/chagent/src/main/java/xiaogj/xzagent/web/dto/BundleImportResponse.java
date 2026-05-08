package xiaogj.xzagent.web.dto;

import java.util.List;
import java.util.Map;

/**
 * Bundle 导入结果响应。
 *
 * <p>该响应既面向前端展示，也可作为后续自动化调用的结构化结果，因此要同时保留：
 * <ul>
 *     <li>阶段级状态</li>
 *     <li>资源级应用结果</li>
 *     <li>问题列表</li>
 * </ul>
 *
 * @param bundleId bundle 标识
 * @param bundleType bundle 类型
 * @param status SUCCESS / PARTIAL / FAILED
 * @param completedStages 已完成阶段
 * @param failedStage 失败阶段
 * @param applied 各资源类型的已应用数量统计
 * @param resources 资源级结果
 * @param issues 导入问题列表
 */
public record BundleImportResponse(
        String bundleId,
        String bundleType,
        String status,
        List<String> completedStages,
        String failedStage,
        Map<String, Integer> applied,
        List<BundleImportResourceResultResponse> resources,
        List<BundleImportIssueResponse> issues) {
}
