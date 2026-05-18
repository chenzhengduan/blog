package xiaogj.xzagent.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * A2A API Key 认证转换器。
 *
 * <p>当前只保护 `POST /xzagent` 这个 JSON-RPC 入口。认证凭据优先从 `X-API-Key`
 * 读取，其次兼容 `Authorization: Bearer sk-...`。
 */
@Component
public class A2aApiKeyAuthenticationConverter implements ServerAuthenticationConverter {

    /**
     * A2A 专用 Header 名称。
     */
    public static final String API_KEY_HEADER = "X-API-Key";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        RequestPath path = exchange.getRequest().getPath();
        if (!"/xzagent".equals(path.value())) {
            return Mono.empty();
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (!StringUtils.hasText(apiKey)) {
            String authorization =
                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
                apiKey = authorization.substring("Bearer ".length()).trim();
            }
        }
        if (!StringUtils.hasText(apiKey)) {
            return Mono.empty();
        }
        return Mono.just(new UsernamePasswordAuthenticationToken("A2A_API_KEY", apiKey));
    }
}
