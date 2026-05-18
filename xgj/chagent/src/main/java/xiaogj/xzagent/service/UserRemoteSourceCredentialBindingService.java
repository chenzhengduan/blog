package xiaogj.xzagent.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.model.UserRemoteSourceCredentialBinding;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import xiaogj.xzagent.repository.UserRemoteSourceCredentialBindingRepository;
import xiaogj.xzagent.web.dto.UserRemoteSourceCredentialBindingResponse;
import xiaogj.xzagent.web.dto.UserRemoteSourceCredentialBindingUpdateRequest;
import org.springframework.stereotype.Service;

/**
 * 用户级远程来源凭证绑定服务。
 *
 * <p>该服务把“用户的第三方凭证”与“全局远程来源”连接起来。运行时只需要知道
 * 当前用户和来源标识，就可以通过这层服务解析出最终应注入的请求头集合。
 */
@Service
public class UserRemoteSourceCredentialBindingService {

    private final UserRemoteSourceCredentialBindingRepository repository;
    private final RemoteToolSourceRepository remoteToolSourceRepository;
    private final ThirdPartyCredentialService thirdPartyCredentialService;

    public UserRemoteSourceCredentialBindingService(
            UserRemoteSourceCredentialBindingRepository repository,
            RemoteToolSourceRepository remoteToolSourceRepository,
            ThirdPartyCredentialService thirdPartyCredentialService) {
        this.repository = repository;
        this.remoteToolSourceRepository = remoteToolSourceRepository;
        this.thirdPartyCredentialService = thirdPartyCredentialService;
    }

    /**
     * 列出某个用户的全部来源绑定。
     */
    public List<UserRemoteSourceCredentialBindingResponse> listBindings(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(binding -> new UserRemoteSourceCredentialBindingResponse(
                        binding.sourceId(),
                        binding.credentialId()))
                .toList();
    }

    /**
     * 保存某个用户对指定来源的凭证绑定。
     */
    public UserRemoteSourceCredentialBindingResponse saveBinding(
            Long userId,
            String sourceId,
            UserRemoteSourceCredentialBindingUpdateRequest request) {
        String normalizedSourceId = requireText(sourceId, "sourceId");
        String normalizedCredentialId = requireText(request.credentialId(), "credentialId");
        validateBindingTarget(userId, normalizedSourceId, normalizedCredentialId);

        UserRemoteSourceCredentialBinding existing = repository.findByUserIdAndSourceId(userId, normalizedSourceId)
                .orElse(null);
        UserRemoteSourceCredentialBinding saved = repository.save(new UserRemoteSourceCredentialBinding(
                existing != null ? existing.id() : null,
                userId,
                normalizedSourceId,
                normalizedCredentialId,
                existing != null ? existing.createdAt() : Instant.now(),
                Instant.now()));
        return new UserRemoteSourceCredentialBindingResponse(
                saved.sourceId(),
                saved.credentialId());
    }

    public void bindIfAbsent(Long userId, String sourceId, String credentialId) {
        String normalizedSourceId = requireText(sourceId, "sourceId");
        String normalizedCredentialId = requireText(credentialId, "credentialId");
        validateBindingTarget(userId, normalizedSourceId, normalizedCredentialId);
        if (repository.findByUserIdAndSourceId(userId, normalizedSourceId).isPresent()) {
            return;
        }
        repository.save(new UserRemoteSourceCredentialBinding(
                null,
                userId,
                normalizedSourceId,
                normalizedCredentialId,
                Instant.now(),
                Instant.now()));
    }

    /**
     * 删除某个用户对指定来源的绑定。
     */
    public void deleteBinding(Long userId, String sourceId) {
        repository.deleteByUserIdAndSourceId(userId, requireText(sourceId, "sourceId"));
    }

    /**
     * 根据用户和来源解析最终应注入的请求头。
     */
    public Map<String, String> resolveHeaders(Long userId, String sourceId) {
        return repository.findByUserIdAndSourceId(userId, sourceId)
                .map(binding -> {
                    if (!thirdPartyCredentialService.existsEnabledCredential(userId, binding.credentialId())) {
                        throw new IllegalStateException(
                                "当前远程来源绑定的第三方凭证不可用，请重新绑定: "
                                        + binding.credentialId());
                    }
                    return thirdPartyCredentialService.resolveHeaders(userId, binding.credentialId());
                })
                .orElse(Collections.emptyMap());
    }

    private void validateBindingTarget(Long userId, String sourceId, String credentialId) {
        boolean sourceExists = remoteToolSourceRepository.findAll().stream()
                .anyMatch(source -> sourceId.equals(source.sourceId()));
        if (!sourceExists) {
            throw new IllegalArgumentException("未找到远程来源: " + sourceId);
        }
        if (!thirdPartyCredentialService.existsEnabledCredential(userId, credentialId)) {
            throw new IllegalArgumentException("未找到可用第三方凭证: " + credentialId);
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value.trim();
    }
}
