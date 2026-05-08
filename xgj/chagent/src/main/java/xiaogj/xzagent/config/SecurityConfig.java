package xiaogj.xzagent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import xiaogj.xzagent.service.JwtService;
import xiaogj.xzagent.service.UserApiKeyService;
import xiaogj.xzagent.service.UserSecurityService;
import xiaogj.xzagent.web.dto.ApiErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * WebFlux 安全配置。
 *
 * <p>当前系统采用 JWT access token 做无状态认证，因此所有请求都通过
 * Bearer Token 恢复登录用户，不使用服务端 Session。
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 密码哈希编码器。
     *
     * <p>登录体系只保存哈希值，绝不保存明文密码。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户级请求头加密器。
     *
     * <p>第一版直接使用 Spring Security 提供的文本加密器，避免为了少量
     * 机密配置再引入独立加密组件。
     */
    @Bean
    public TextEncryptor textEncryptor(XzagentSecurityProperties properties) {
        return Encryptors.delux(
                properties.headerEncryptionPassword(),
                properties.headerEncryptionSalt());
    }

    /**
     * 认证过滤链。
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveAuthenticationManager authenticationManager,
            JwtAuthenticationConverter authenticationConverter,
            UserApiKeyService userApiKeyService,
            A2aApiKeyAuthenticationConverter a2aApiKeyAuthenticationConverter,
            CorsConfigurationSource corsConfigurationSource) {
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authenticationManager);
        jwtFilter.setServerAuthenticationConverter(authenticationConverter);
        jwtFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers("/xzagent/api/**", "/xzagent/agui/**"));
        jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        AuthenticationWebFilter a2aApiKeyFilter =
                new AuthenticationWebFilter(new A2aApiKeyAuthenticationManager(userApiKeyService));
        a2aApiKeyFilter.setServerAuthenticationConverter(a2aApiKeyAuthenticationConverter);
        a2aApiKeyFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/xzagent"));
        a2aApiKeyFilter.setSecurityContextRepository(
                NoOpServerSecurityContextRepository.getInstance());

        return http
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint((exchange, ex) ->
                                writeError(exchange, HttpStatus.UNAUTHORIZED, "未登录或登录已失效"))
                        .accessDeniedHandler((exchange, ex) ->
                                writeError(exchange, HttpStatus.FORBIDDEN, "没有权限访问该资源")))
                .addFilterAt(a2aApiKeyFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(spec -> spec
                        .pathMatchers(
                                "/xzagent/api/auth/bootstrap/status",
                                "/xzagent/api/auth/bootstrap",
                                "/xzagent/api/auth/login",
                                "/xzagent/private/api/auth/plugin-login")
                        .permitAll()
                        .pathMatchers(HttpMethod.POST, "/xzagent")
                        .authenticated()
                        .pathMatchers("/xzagent/api/admin/**")
                        .hasAuthority("ROLE_SUPER_ADMIN")
                        .pathMatchers(
                                "/xzagent/api/auth/me",
                                "/xzagent/api/auth/logout",
                                "/xzagent/api/me/**",
                                "/xzagent/agui/**",
                                "/xzagent/api/sessions/**")
                        .authenticated()
                        .pathMatchers("/xzagent/api/**")
                        .authenticated()
                        .anyExchange()
                        .permitAll())
                .build();
    }

    /**
     * 跨域配置。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(XzagentSecurityProperties properties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        XzagentSecurityProperties.Cors cors = properties.cors();
        if (cors == null || !cors.enabled()) {
            return source;
        }
        CorsConfiguration configuration = new CorsConfiguration();
        if (!cors.allowedOrigins().isEmpty()) {
            configuration.setAllowedOrigins(cors.allowedOrigins());
        }
        if (!cors.allowedOriginPatterns().isEmpty()) {
            configuration.setAllowedOriginPatterns(cors.allowedOriginPatterns());
        }
        if (!cors.allowedMethods().isEmpty()) {
            configuration.setAllowedMethods(cors.allowedMethods());
        }
        if (!cors.allowedHeaders().isEmpty()) {
            configuration.setAllowedHeaders(cors.allowedHeaders());
        }
        if (!cors.exposedHeaders().isEmpty()) {
            configuration.setExposedHeaders(cors.exposedHeaders());
        }
        configuration.setAllowCredentials(cors.allowCredentials());
        configuration.setMaxAge(cors.maxAgeSeconds());
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * JWT 转认证对象管理器。
     */
    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
            JwtService jwtService,
            UserSecurityService userSecurityService) {
        return authentication -> {
            if (!(authentication instanceof UsernamePasswordAuthenticationToken tokenAuth)) {
                return Mono.empty();
            }
            String token = tokenAuth.getCredentials().toString();
            String username = jwtService.extractUsername(token);
            return userSecurityService.loadUserByUsername(username)
                    .filter(user -> jwtService.isTokenValid(token, user.username()))
                    .map(user -> new UsernamePasswordAuthenticationToken(
                            user,
                            token,
                            List.copyOf(user.authorities())));
        };
    }

    private Mono<Void> writeError(
            ServerWebExchange exchange,
            HttpStatus status,
            String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ApiErrorResponse body = new ApiErrorResponse(
                java.time.Instant.now(),
                exchange.getRequest().getPath().value(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getId(),
                List.of());
        try {
            byte[] json = objectMapper.writeValueAsString(body)
                    .getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(
                    exchange.getResponse().bufferFactory().wrap(json)));
        } catch (Exception exception) {
            exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_LENGTH, "0");
            return exchange.getResponse().setComplete();
        }
    }
}
