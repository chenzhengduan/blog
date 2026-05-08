package xiaogj.xzagent.service;

import java.time.Instant;
import java.util.UUID;
import xiaogj.xzagent.infrastructure.session.MysqlAgentSession;
import xiaogj.xzagent.model.SysUser;
import xiaogj.xzagent.model.UserRole;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import xiaogj.xzagent.repository.SysUserRepository;
import xiaogj.xzagent.web.dto.AuthLoginResponse;
import xiaogj.xzagent.web.dto.PluginLoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PluginLoginService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ThirdPartyCredentialService thirdPartyCredentialService;
    private final UserRemoteSourceCredentialBindingService bindingService;
    private final RemoteToolSourceRepository remoteToolSourceRepository;
    private final AuthService authService;
    private final MysqlAgentSession mysqlAgentSession;

    public PluginLoginService(
            SysUserRepository sysUserRepository,
            PasswordEncoder passwordEncoder,
            ThirdPartyCredentialService thirdPartyCredentialService,
            UserRemoteSourceCredentialBindingService bindingService,
            RemoteToolSourceRepository remoteToolSourceRepository,
            AuthService authService,
            MysqlAgentSession mysqlAgentSession) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.thirdPartyCredentialService = thirdPartyCredentialService;
        this.bindingService = bindingService;
        this.remoteToolSourceRepository = remoteToolSourceRepository;
        this.authService = authService;
        this.mysqlAgentSession = mysqlAgentSession;
    }

    public AuthLoginResponse loginOrRegister(PluginLoginRequest request) {
        String uid = requireText(request.uid(), "uid");
        String credentialId = requireText(request.thirdPartyCredentialId(), "thirdPartyCredentialId");

        SysUser user = sysUserRepository.findByUsername(uid)
                .orElseGet(() -> createUser(uid));
        if (!user.enabled()) {
            throw new IllegalArgumentException("该用户已被禁用，无法登录");
        }

        thirdPartyCredentialService.upsertCredentialHeaders(user.id(), credentialId, request.thirdPartyCredentialHeaders());
        remoteToolSourceRepository.findAll().forEach(source ->
                bindingService.bindIfAbsent(user.id(), source.sourceId(), credentialId));

        AuthLoginResponse base = authService.issueLoginResponse(user);
        String latestSessionId = mysqlAgentSession.findLatestSessionIdByUserId(user.id()).orElse(null);
        return new AuthLoginResponse(base.accessToken(), base.user(), latestSessionId);
    }

    private SysUser createUser(String username) {
        Instant now = Instant.now();
        return sysUserRepository.save(new SysUser(
                null,
                username,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                UserRole.USER,
                true,
                now,
                now));
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value.trim();
    }
}
