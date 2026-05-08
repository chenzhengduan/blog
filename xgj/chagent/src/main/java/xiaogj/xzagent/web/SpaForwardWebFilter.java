package xiaogj.xzagent.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 将前端 UI 路径映射到 Flutter Web 产物。
 *
 * <p>UI 前缀由 {@code xzagent.ui.path-prefix} 配置，默认 {@code /xzagent/ui}。
 * 经 Zuul 网关透传时可通过 Nacos 将其覆盖为 {@code /api/xzagent/ui}。
 */
@Component
public class SpaForwardWebFilter implements WebFilter {

    /** UI 路径前缀，例如 {@code /xzagent/ui} 或 {@code /api/xzagent/ui}。 */
    private final String uiPathPrefix;

    public SpaForwardWebFilter(
            @Value("${xzagent.ui.path-prefix:/xzagent/ui}") String uiPathPrefix) {
        // 去掉末尾斜杠，统一内部比较格式
        this.uiPathPrefix = uiPathPrefix.replaceAll("/+$", "");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (request.getMethod() != HttpMethod.GET) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();
        String prefix = uiPathPrefix;
        String prefixSlash = prefix + "/";
        String contextPath = request.getPath().contextPath().value();

        // 静态资源文件（含扩展名）：剥离 UI 前缀后交给静态资源处理器。
        // 这里必须保留 contextPath，否则在经网关透传且带 context-path 时会触发
        // "contextPath must match the start of requestPath" 异常。
        if (path.startsWith(prefixSlash)) {
            String subPath = path.substring(prefix.length());
            if (subPath.contains(".")) {
                ServerHttpRequest staticRequest = request.mutate()
                        .path(contextPath + subPath)
                        .build();
                return chain.filter(exchange.mutate().request(staticRequest).build());
            }
        }

        // UI 页面路径（深链）：一律回退到 index.html 由前端路由处理。
        if (prefix.equals(path) || path.startsWith(prefixSlash)) {
            ServerHttpRequest forwardedRequest = request.mutate()
                    .path(contextPath + "/index.html")
                    .build();
            return chain.filter(exchange.mutate().request(forwardedRequest).build());
        }

        return chain.filter(exchange);
    }
}
