package xiaogj.xzagent.tool;

/**
 * 工具执行时的用户上下文，通过 {@code ToolkitConfig.defaultContext} 在服务端注入，
 * AI（LLM）完全不可见，仅在 {@link io.agentscope.core.tool.AgentTool#callAsync} 内可取到。
 *
 * <p>取用方式：
 * <pre>{@code
 * UserToolContext userCtx = param.getContext().get(UserToolContext.class);
 * }</pre>
 *
 * <p>字段按需扩展，当前承载用户 ID 和用户名，未来可增加租户、权限等信息。
 */
public class UserToolContext {

    /** 当前请求的用户 ID。 */
    private final Long userId;

    /** 当前请求的用户名。 */
    private final String username;

    /**
     * @param userId   用户 ID
     * @param username 用户名
     */
    public UserToolContext(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    /** 获取用户 ID。 */
    public Long getUserId() {
        return userId;
    }

    /** 获取用户名。 */
    public String getUsername() {
        return username;
    }
}
