package xiaogj.xzagent.repository;

import java.util.List;
import java.util.Optional;
import xiaogj.xzagent.model.UserRemoteSourceCredentialBinding;

/**
 * 用户级远程来源凭证绑定仓储。
 */
public interface UserRemoteSourceCredentialBindingRepository {

    /**
     * 查询某个用户对某个来源的绑定。
     */
    Optional<UserRemoteSourceCredentialBinding> findByUserIdAndSourceId(Long userId, String sourceId);

    /**
     * 列出某个用户的全部绑定。
     */
    List<UserRemoteSourceCredentialBinding> findByUserId(Long userId);

    /**
     * 新增或更新绑定。
     */
    UserRemoteSourceCredentialBinding save(UserRemoteSourceCredentialBinding binding);

    /**
     * 删除某个用户对某个来源的绑定。
     */
    void deleteByUserIdAndSourceId(Long userId, String sourceId);

    /**
     * 删除某个用户下指向指定凭证的全部绑定。
     */
    void deleteByUserIdAndCredentialId(Long userId, String credentialId);

    /**
     * 删除某个来源下的全部用户绑定。
     */
    void deleteBySourceId(String sourceId);

    /**
     * 删除某个用户的全部来源绑定。
     */
    void deleteByUserId(Long userId);

    /**
     * 判断某个用户下是否仍有来源绑定引用该凭证。
     */
    boolean existsByUserIdAndCredentialId(Long userId, String credentialId);
}
