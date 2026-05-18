package xiaogj.xzagent.web.dto;

/**
 * 登录成功响应。
 */
public record AuthLoginResponse(
        String accessToken,
        AuthUserResponse user,
        String latestSessionId) {
}
