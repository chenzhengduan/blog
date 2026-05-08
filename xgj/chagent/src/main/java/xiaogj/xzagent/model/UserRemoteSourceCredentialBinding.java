package xiaogj.xzagent.model;

import java.time.Instant;

/**
 * 用户级远程来源凭证绑定。
 *
 * <p>绑定关系的语义是：同一个全局远程来源，对不同用户可以绑定不同凭证。
 * 第一版限定为“每个用户对每个来源最多绑定一套凭证”，以降低复杂度。
 *
 * @param id 主键
 * @param userId 所属用户
 * @param sourceId 全局远程来源标识
 * @param credentialId 当前用户为该来源选择的凭证标识
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record UserRemoteSourceCredentialBinding(
        Long id,
        Long userId,
        String sourceId,
        String credentialId,
        Instant createdAt,
        Instant updatedAt) {
}
