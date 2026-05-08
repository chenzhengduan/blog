package xiaogj.xzagent.util;

import java.util.regex.Pattern;

/**
 * 会话标识校验工具。
 */
public final class SessionIdValidator {

    private static final Pattern SAFE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");

    private SessionIdValidator() {
    }

    /**
     * 校验并返回安全的 sessionId。
     *
     * <p>当前只允许字母、数字、下划线和短横线，避免会话标识被用于
     * SQL、路径、日志等位置时引入注入或污染风险。
     */
    public static String validate(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        if (!SAFE_PATTERN.matcher(sessionId).matches()) {
            throw new IllegalArgumentException("sessionId 只允许字母、数字、下划线和短横线");
        }
        return sessionId;
    }
}
