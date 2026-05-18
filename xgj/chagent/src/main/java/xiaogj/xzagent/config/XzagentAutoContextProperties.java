package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * AutoContextMemory 配置。
 *
 * @param largePayloadThreshold 大 payload 触发 offload 的字符阈值
 * @param maxToken AutoContext 工作上下文的最大 token 预算
 * @param tokenRatio 达到 maxToken 的比例后触发压缩
 * @param offloadSinglePreview 单条 offload 内容保留的预览字符数
 * @param msgThreshold 消息条数达到该阈值后触发压缩
 * @param lastKeep 最近保留为原文的消息条数
 * @param minConsecutiveToolMessages 连续 tool message 参与压缩的最小条数
 * @param currentRoundCompressionRatio 当前轮压缩目标比例
 * @param minCompressionTokenThreshold 小于该 token 数时跳过压缩
 */
@Validated
@ConfigurationProperties(prefix = "xzagent.auto-context")
public record XzagentAutoContextProperties(
        long largePayloadThreshold,
        long maxToken,
        double tokenRatio,
        int offloadSinglePreview,
        int msgThreshold,
        int lastKeep,
        int minConsecutiveToolMessages,
        double currentRoundCompressionRatio,
        int minCompressionTokenThreshold) {
}
