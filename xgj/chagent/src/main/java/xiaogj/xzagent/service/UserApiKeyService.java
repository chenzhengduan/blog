package xiaogj.xzagent.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import xiaogj.xzagent.config.XzagentSecurityProperties;
import xiaogj.xzagent.model.SysUser;
import xiaogj.xzagent.model.UserApiKey;
import xiaogj.xzagent.repository.SysUserRepository;
import xiaogj.xzagent.repository.UserApiKeyRepository;
import xiaogj.xzagent.web.dto.UserApiKeyCreateRequest;
import xiaogj.xzagent.web.dto.UserApiKeyResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户 API Key 服务。
 *
 * <p>该服务统一负责以下职责：
 * <ul>
 *     <li>生成与重置用户唯一 API Key</li>
 *     <li>返回当前用户可展示的 Key 状态</li>
 *     <li>校验外部传入的 A2A API Key 并还原为用户身份</li>
 * </ul>
 *
 * <p>数据库中永远只保存 Key 哈希，不保存完整明文。明文只在生成成功时返回一次。
 */
@Service
public class UserApiKeyService {

    private final UserApiKeyRepository userApiKeyRepository;
    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final XzagentSecurityProperties securityProperties;

    public UserApiKeyService(
            UserApiKeyRepository userApiKeyRepository,
            SysUserRepository sysUserRepository,
            PasswordEncoder passwordEncoder,
            XzagentSecurityProperties securityProperties) {
        this.userApiKeyRepository = userApiKeyRepository;
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }

    /**
     * 读取当前用户 API Key 状态。
     */
    public UserApiKeyResponse getCurrentUserApiKey(Long userId) {
        return userApiKeyRepository.findByUserId(userId)
                .map(apiKey -> toResponse(apiKey, null))
                .orElse(new UserApiKeyResponse(
                        false,
                        null,
                        null,
                        false,
                        false,
                        null,
                        null,
                        null));
    }

    /**
     * 为当前用户创建或重置唯一 API Key。
     *
     * <p>同一用户只能存在一条记录，因此这里会覆盖旧 Key。旧 Key 一旦被重置，
     * 立刻失效。
     */
    public UserApiKeyResponse createOrRotateCurrentUserApiKey(
            Long userId,
            UserApiKeyCreateRequest request) {
        validateCreateRequest(request);

        String plainTextKey = generatePlainTextKey();
        Instant now = Instant.now();
        UserApiKey apiKey = userApiKeyRepository.findByUserId(userId)
                .map(existing -> new UserApiKey(
                        existing.id(),
                        userId,
                        buildLookupPrefix(plainTextKey),
                        passwordEncoder.encode(plainTextKey),
                        request.permanent() ? null : request.expiresAt(),
                        request.permanent(),
                        true,
                        existing.createdAt(),
                        now))
                .orElse(new UserApiKey(
                        null,
                        userId,
                        buildLookupPrefix(plainTextKey),
                        passwordEncoder.encode(plainTextKey),
                        request.permanent() ? null : request.expiresAt(),
                        request.permanent(),
                        true,
                        now,
                        now));
        UserApiKey saved = userApiKeyRepository.save(apiKey);
        return toResponse(saved, plainTextKey);
    }

    /**
     * 删除当前用户 API Key。
     */
    public void deleteCurrentUserApiKey(Long userId) {
        userApiKeyRepository.deleteByUserId(userId);
    }

    /**
     * 根据外部 API Key 解析登录用户。
     *
     * <p>A2A 请求使用该方法完成服务端认证。解析成功后返回用户主体，失败则返回空。
     */
    public Optional<UserPrincipal> authenticateByApiKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }
        if (!rawKey.startsWith(securityProperties.apiKeyPrefix())) {
            return Optional.empty();
        }
        String lookupPrefix = buildLookupPrefix(rawKey);
        Instant now = Instant.now();
        return userApiKeyRepository.findByKeyPrefix(lookupPrefix).stream()
                .filter(UserApiKey::enabled)
                .filter(apiKey -> apiKey.permanent()
                        || (apiKey.expiresAt() != null && apiKey.expiresAt().isAfter(now)))
                .filter(apiKey -> passwordEncoder.matches(rawKey, apiKey.keyHash()))
                .findFirst()
                .flatMap(apiKey -> sysUserRepository.findById(apiKey.userId()))
                .filter(SysUser::enabled)
                .map(user -> new UserPrincipal(
                        user.id(),
                        user.username(),
                        user.role(),
                        user.enabled()));
    }

    /**
     * 删除指定用户的 API Key。
     *
     * <p>用于后台删除用户时顺手清理遗留凭据。
     */
    public void deleteByUserId(Long userId) {
        userApiKeyRepository.deleteByUserId(userId);
    }

    private void validateCreateRequest(UserApiKeyCreateRequest request) {
        if (!request.permanent() && request.expiresAt() == null) {
            throw new IllegalArgumentException("非永久 API Key 必须设置过期时间");
        }
    }

    private String generatePlainTextKey() {
        String random = UUID.randomUUID().toString().replace("-", "");
        return securityProperties.apiKeyPrefix() + random;
    }

    private String buildLookupPrefix(String plainTextKey) {
        int length = Math.min(
                plainTextKey.length(),
                Math.max(4, securityProperties.apiKeyLookupPrefixLength()));
        return plainTextKey.substring(0, length);
    }

    private UserApiKeyResponse toResponse(UserApiKey apiKey, String plainTextKey) {
        return new UserApiKeyResponse(
                true,
                maskKey(apiKey.keyPrefix()),
                plainTextKey,
                apiKey.permanent(),
                apiKey.enabled(),
                apiKey.expiresAt(),
                apiKey.createdAt(),
                apiKey.updatedAt());
    }

    private String maskKey(String keyPrefix) {
        return keyPrefix + "****************";
    }
}
