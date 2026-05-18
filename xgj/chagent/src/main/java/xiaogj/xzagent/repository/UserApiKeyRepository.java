package xiaogj.xzagent.repository;

import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.UserApiKey;

/**
 * 用户 API Key 仓储。
 */
public interface UserApiKeyRepository {

    /**
     * 按用户查询其唯一 API Key。
     */
    Optional<UserApiKey> findByUserId(Long userId);

    /**
     * 按前缀查询候选 API Key。
     *
     * <p>前缀用于缩小哈希匹配范围，真正认证仍以哈希校验为准。
     */
    List<UserApiKey> findByKeyPrefix(String keyPrefix);

    /**
     * 新增或更新 API Key。
     */
    UserApiKey save(UserApiKey apiKey);

    /**
     * 删除某个用户的 API Key。
     */
    void deleteByUserId(Long userId);
}
