package xiaogj.xzagent.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import xiaogj.xzagent.model.ThirdPartyCredential;
import xiaogj.xzagent.repository.ThirdPartyCredentialRepository;
import xiaogj.xzagent.repository.UserRemoteSourceCredentialBindingRepository;
import xiaogj.xzagent.web.dto.ThirdPartyCredentialCreateRequest;
import xiaogj.xzagent.web.dto.ThirdPartyCredentialResponse;
import xiaogj.xzagent.web.dto.ThirdPartyCredentialUpdateRequest;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

/**
 * 用户级第三方凭证服务。
 *
 * <p>该服务负责第三方凭证的增删改查，以及请求头 JSON 的加密存储与解密读取。
 * 与旧“我的请求头”不同，凭证是可复用对象，运行时并不直接按来源读取请求头。
 */
@Service
public class ThirdPartyCredentialService {

    private final ThirdPartyCredentialRepository repository;
    private final UserRemoteSourceCredentialBindingRepository bindingRepository;
    private final ObjectMapper objectMapper;
    private final TextEncryptor textEncryptor;

    public ThirdPartyCredentialService(
            ThirdPartyCredentialRepository repository,
            UserRemoteSourceCredentialBindingRepository bindingRepository,
            ObjectMapper objectMapper,
            TextEncryptor textEncryptor) {
        this.repository = repository;
        this.bindingRepository = bindingRepository;
        this.objectMapper = objectMapper;
        this.textEncryptor = textEncryptor;
    }

    /**
     * 列出某个用户的全部凭证。
     */
    public List<ThirdPartyCredentialResponse> listCredentials(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 创建新凭证。
     */
    public ThirdPartyCredentialResponse createCredential(
            Long userId,
            ThirdPartyCredentialCreateRequest request) {
        String normalizedCredentialId = requireText(request.credentialId(), "credentialId");
        if (repository.findByUserIdAndCredentialId(userId, normalizedCredentialId).isPresent()) {
            throw new IllegalArgumentException("凭证标识已存在: " + normalizedCredentialId);
        }
        Instant now = Instant.now();
        ThirdPartyCredential saved = repository.save(new ThirdPartyCredential(
                null,
                userId,
                normalizedCredentialId,
                requireText(request.name(), "name"),
                normalizeNullableText(request.description()),
                encryptHeaders(normalizeHeaders(request.headers())),
                request.enabled() == null || request.enabled(),
                now,
                now));
        return toResponse(saved);
    }

    /**
     * 更新已有凭证。
     */
    public ThirdPartyCredentialResponse updateCredential(
            Long userId,
            String credentialId,
            ThirdPartyCredentialUpdateRequest request) {
        String normalizedCredentialId = requireText(credentialId, "credentialId");
        ThirdPartyCredential existing = repository.findByUserIdAndCredentialId(
                        userId,
                        normalizedCredentialId)
                .orElseThrow(() -> new IllegalArgumentException("未找到第三方凭证: " + credentialId));
        boolean nextEnabled = request.enabled() == null ? existing.enabled() : request.enabled();
        if (!nextEnabled
                && existing.enabled()
                && bindingRepository.existsByUserIdAndCredentialId(userId, normalizedCredentialId)) {
            throw new IllegalArgumentException("该凭证仍被来源绑定，请先解除绑定后再停用");
        }
        ThirdPartyCredential saved = repository.save(new ThirdPartyCredential(
                existing.id(),
                existing.userId(),
                existing.credentialId(),
                requireText(request.name(), "name"),
                normalizeNullableText(request.description()),
                encryptHeaders(normalizeHeaders(request.headers())),
                nextEnabled,
                existing.createdAt(),
                Instant.now()));
        return toResponse(saved);
    }

    /**
     * 删除凭证。
     *
     * <p>删除凭证时，会同时清理当前用户下所有引用该凭证的来源绑定，避免悬空引用。
     */
    public void deleteCredential(Long userId, String credentialId) {
        String normalizedCredentialId = requireText(credentialId, "credentialId");
        bindingRepository.deleteByUserIdAndCredentialId(userId, normalizedCredentialId);
        repository.deleteByUserIdAndCredentialId(userId, normalizedCredentialId);
    }

    /**
     * 解析凭证里的请求头。
     */
    public Map<String, String> resolveHeaders(Long userId, String credentialId) {
        return repository.findByUserIdAndCredentialId(userId, credentialId)
                .filter(ThirdPartyCredential::enabled)
                .map(ThirdPartyCredential::headersJsonEncrypted)
                .map(this::decryptHeaders)
                .orElse(Collections.emptyMap());
    }

    /**
     * 判断某个用户是否拥有指定且启用中的凭证。
     */
    public boolean existsEnabledCredential(Long userId, String credentialId) {
        return repository.findByUserIdAndCredentialId(userId, credentialId)
                .filter(ThirdPartyCredential::enabled)
                .isPresent();
    }

    public ThirdPartyCredentialResponse upsertCredentialValue(
            Long userId,
            String credentialId,
            String credentialValue) {
        String normalizedCredentialId = requireText(credentialId, "credentialId");
        String normalizedCredentialValue = requireText(credentialValue, "credentialValue");
        return upsertCredentialHeaders(userId, normalizedCredentialId, Map.of("credentialValue", normalizedCredentialValue));
    }

    public ThirdPartyCredentialResponse upsertCredentialHeaders(
            Long userId,
            String credentialId,
            Map<String, String> headers) {
        String normalizedCredentialId = requireText(credentialId, "credentialId");
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("thirdPartyCredentialHeaders 不能为空");
        }
        ThirdPartyCredential existing = repository.findByUserIdAndCredentialId(userId, normalizedCredentialId)
                .orElse(null);
        ThirdPartyCredential saved = repository.save(new ThirdPartyCredential(
                existing != null ? existing.id() : null,
                userId,
                normalizedCredentialId,
                normalizedCredentialId,
                existing != null ? existing.description() : null,
                encryptHeaders(headers),
                true,
                existing != null ? existing.createdAt() : Instant.now(),
                Instant.now()));
        return toResponse(saved);
    }

    private ThirdPartyCredentialResponse toResponse(ThirdPartyCredential credential) {
        return new ThirdPartyCredentialResponse(
                credential.credentialId(),
                credential.name(),
                credential.description(),
                decryptHeaders(credential.headersJsonEncrypted()),
                credential.enabled(),
                credential.createdAt(),
                credential.updatedAt());
    }

    private Map<String, String> normalizeHeaders(Map<String, String> headers) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (headers == null || headers.isEmpty()) {
            return normalized;
        }
        headers.forEach((key, value) -> {
            if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
                normalized.put(key.trim(), value.trim());
            }
        });
        return normalized;
    }

    private String encryptHeaders(Map<String, String> headers) {
        try {
            return textEncryptor.encrypt(objectMapper.writeValueAsString(headers));
        } catch (IOException e) {
            throw new IllegalStateException("无法序列化第三方凭证请求头", e);
        }
    }

    private Map<String, String> decryptHeaders(String encrypted) {
        try {
            return objectMapper.readValue(
                    textEncryptor.decrypt(encrypted),
                    new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("无法解析第三方凭证请求头", e);
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value.trim();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
