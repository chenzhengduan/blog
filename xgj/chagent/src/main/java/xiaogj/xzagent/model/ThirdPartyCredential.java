package xiaogj.xzagent.model;

import java.time.Instant;

/**
 * 用户级第三方凭证。
 *
 * <p>该对象表示用户维护的一套可复用请求头集合。与旧的“来源直接存请求头”
 * 模型不同，这里先把凭证抽象成独立对象，再由用户把远程来源绑定到某个凭证。
 *
 * @param id 主键
 * @param userId 所属用户
 * @param credentialId 用户可读的稳定标识
 * @param name 展示名称
 * @param description 描述说明
 * @param headersJsonEncrypted 加密后的请求头 JSON
 * @param enabled 是否启用
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record ThirdPartyCredential(
        Long id,
        Long userId,
        String credentialId,
        String name,
        String description,
        String headersJsonEncrypted,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt) {
}
