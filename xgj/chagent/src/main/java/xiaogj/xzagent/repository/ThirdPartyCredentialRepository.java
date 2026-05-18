package xiaogj.xzagent.repository;

import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.ThirdPartyCredential;

/**
 * 用户级第三方凭证仓储。
 */
public interface ThirdPartyCredentialRepository {

    /**
     * 查询某个用户名下的指定凭证。
     */
    Optional<ThirdPartyCredential> findByUserIdAndCredentialId(Long userId, String credentialId);

    /**
     * 列出某个用户的全部凭证。
     */
    List<ThirdPartyCredential> findByUserId(Long userId);

    /**
     * 新增或更新凭证。
     */
    ThirdPartyCredential save(ThirdPartyCredential credential);

    /**
     * 删除某个用户名下的指定凭证。
     */
    void deleteByUserIdAndCredentialId(Long userId, String credentialId);

    /**
     * 删除某个用户的全部凭证。
     */
    void deleteByUserId(Long userId);
}
