package xiaogj.xzagent.model;

/**
 * 图片生成用量明细，对应 image_generate_usage 表。
 *
 * <p>每次图片生成工具成功完成后写入一条记录，按实际成功上传的图片张数统计，
 * 用于按用户、按供应商、按模型维度做图片能力计费与对账。
 *
 * @param userId 用户主键，来自认证上下文
 * @param sessionId 会话标识，关联 agent_session.session_id
 * @param runId 运行标识，关联 agent_run.run_id
 * @param provider 图片供应商标识，如 dashscope、openai
 * @param model 图片模型名称，如 wan2.7-image、dall-e-3
 * @param size 本次请求的目标尺寸，如 1024x1024
 * @param imageCount 实际成功上传并返回持久化 URL 的图片张数
 */
public record ImageGenerateUsage(
        Long userId,
        String sessionId,
        String runId,
        String provider,
        String model,
        String size,
        int imageCount) {
}
