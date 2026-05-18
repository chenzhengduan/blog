package xiaogj.xzagent.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 把 Bearer Token 请求头转换为待认证对象。
 *
 * <p>优先读标准 {@code Authorization: Bearer <token>} 头；
 * 若不存在则读自定义 {@code xzagent-authorization: Bearer <token>} 头。
 * 自定义头用于经 Zuul 网关转发的场景——Zuul 默认将 {@code Authorization} 列为
 * sensitiveHeaders 而不透传，自定义头可绕过此限制。
 */
@Component
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    /** 自定义 token 头，供经 Zuul 等网关转发时使用。 */
    static final String XZAGENT_AUTHORIZATION_HEADER = "xzagent-authorization";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String token = extractToken(exchange);
        if (token == null || token.isEmpty()) {
            return Mono.empty();
        }
        return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
    }

    private String extractToken(ServerWebExchange exchange) {
        // 优先读标准 Authorization 头
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // 降级读自定义头（用于 Zuul 网关场景）
            authorization = exchange.getRequest().getHeaders().getFirst(XZAGENT_AUTHORIZATION_HEADER);
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
