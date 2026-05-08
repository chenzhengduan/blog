package xiaogj.xzagent.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import xiaogj.xzagent.config.XzagentSecurityProperties;
import org.springframework.stereotype.Service;

/**
 * JWT 生成与校验服务。
 *
 * <p>第一版只发 access token，不引入 refresh token 或黑名单机制。
 */
@Service
public class JwtService {

    private final XzagentSecurityProperties securityProperties;
    private final SecretKey secretKey;

    public JwtService(XzagentSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                securityProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 为指定用户签发访问令牌。
     */
    public String generateToken(UserPrincipal user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(
                securityProperties.jwtExpireMinutes(),
                ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(user.username())
                .claim("role", user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 提取用户名。
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 判断 token 是否仍然有效。
     */
    public boolean isTokenValid(String token, String expectedUsername) {
        Claims claims = parseClaims(token);
        return expectedUsername.equals(claims.getSubject())
                && claims.getExpiration() != null
                && claims.getExpiration().toInstant().isAfter(Instant.now());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
