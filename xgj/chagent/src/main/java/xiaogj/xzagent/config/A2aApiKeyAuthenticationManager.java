package xiaogj.xzagent.config;

import xiaogj.xzagent.service.UserApiKeyService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 * A2A API Key 认证管理器。
 *
 * <p>该管理器只负责把原始 API Key 解析成系统用户，不参与网页 JWT 认证。
 */
public class A2aApiKeyAuthenticationManager implements ReactiveAuthenticationManager {

    private final UserApiKeyService userApiKeyService;

    public A2aApiKeyAuthenticationManager(UserApiKeyService userApiKeyService) {
        this.userApiKeyService = userApiKeyService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken tokenAuth)) {
            return Mono.empty();
        }
        String rawKey = String.valueOf(tokenAuth.getCredentials());
        return Mono.justOrEmpty(userApiKeyService.authenticateByApiKey(rawKey))
                .map(user -> new UsernamePasswordAuthenticationToken(
                        user,
                        rawKey,
                        user.authorities()));
    }
}
