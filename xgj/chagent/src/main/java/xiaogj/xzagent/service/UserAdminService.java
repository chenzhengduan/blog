package xiaogj.xzagent.service;

import java.time.Instant;
import java.util.List;
import xiaogj.xzagent.model.SysUser;
import xiaogj.xzagent.model.UserRole;
import xiaogj.xzagent.repository.SysUserRepository;
import xiaogj.xzagent.repository.ThirdPartyCredentialRepository;
import xiaogj.xzagent.repository.UserRemoteSourceCredentialBindingRepository;
import xiaogj.xzagent.web.dto.AuthUserResponse;
import xiaogj.xzagent.web.dto.UserUpsertRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户管理服务。
 */
@Service
public class UserAdminService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserApiKeyService userApiKeyService;
    private final ThirdPartyCredentialRepository thirdPartyCredentialRepository;
    private final UserRemoteSourceCredentialBindingRepository bindingRepository;

    public UserAdminService(
            SysUserRepository sysUserRepository,
            PasswordEncoder passwordEncoder,
            UserApiKeyService userApiKeyService,
            ThirdPartyCredentialRepository thirdPartyCredentialRepository,
            UserRemoteSourceCredentialBindingRepository bindingRepository) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userApiKeyService = userApiKeyService;
        this.thirdPartyCredentialRepository = thirdPartyCredentialRepository;
        this.bindingRepository = bindingRepository;
    }

    /**
     * 列出全部用户。
     */
    public List<AuthUserResponse> listUsers() {
        return sysUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 新增用户。
     */
    public AuthUserResponse createUser(UserUpsertRequest request) {
        if (sysUserRepository.findByUsername(request.username().trim()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        SysUser user = new SysUser(
                null,
                request.username().trim(),
                passwordEncoder.encode(request.password()),
                UserRole.valueOf(request.role()),
                request.enabled(),
                Instant.now(),
                Instant.now());
        return toResponse(sysUserRepository.save(user));
    }

    /**
     * 删除用户。
     *
     * <p>为了避免系统失管，当前唯一超管不能被删除。
     */
    public void deleteUser(Long userId) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("未找到用户"));
        if (user.role() == UserRole.SUPER_ADMIN
                && sysUserRepository.countByRole(UserRole.SUPER_ADMIN) <= 1) {
            throw new IllegalArgumentException("当前唯一超管不能删除");
        }
        // 用户删除时同步删除其 API Key，避免遗留无主凭据继续可用。
        userApiKeyService.deleteByUserId(userId);
        bindingRepository.deleteByUserId(userId);
        thirdPartyCredentialRepository.deleteByUserId(userId);
        sysUserRepository.deleteById(userId);
    }

    private AuthUserResponse toResponse(SysUser user) {
        return new AuthUserResponse(
                user.id(),
                user.username(),
                user.role().name(),
                user.enabled());
    }
}
