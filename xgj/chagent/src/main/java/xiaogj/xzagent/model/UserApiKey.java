package xiaogj.xzagent.model;

import java.time.Instant;

/**
 * 用户 API Key 定义。
 *
 * <p>该对象对应 `user_api_key` 表，用于承载“一个用户一个 A2A API Key”
 * 这条安全策略。数据库只保存哈希和可检索前缀，不保存完整明文 Key。
 *
 * @param id 主键
 * @param userId 所属用户主键
 * @param keyPrefix 用于快速定位候选记录的前缀
 * @param keyHash API Key 哈希值
 * @param expiresAt 过期时间，永久 Key 时为 null
 * @param permanent 是否永久有效
 * @param enabled 是否启用
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record UserApiKey(
        Long id,
        Long userId,
        String keyPrefix,
        String keyHash,
        Instant expiresAt,
        boolean permanent,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt) {
}
