package xiaogj.xzagent.web.dto;

/**
 * 当前登录用户响应。
 */
public record AuthUserResponse(
        Long id,
        String username,
        String role,
        boolean enabled) {
}
