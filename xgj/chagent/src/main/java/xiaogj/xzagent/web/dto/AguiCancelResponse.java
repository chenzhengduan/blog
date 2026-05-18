package xiaogj.xzagent.web.dto;

/**
 * Agent 运行取消的响应体。
 *
 * @param cancelled 是否成功取消了正在运行的 Agent；
 *                  false 表示该 session 当前无活跃运行（操作幂等，不视为错误）
 * @param code      业务状态码：OK 表示已取消，NO_ACTIVE_RUN 表示无活跃运行
 * @param message   可读描述
 */
public record AguiCancelResponse(boolean cancelled, String code, String message) {

    /** 取消成功 */
    public static AguiCancelResponse ok() {
        return new AguiCancelResponse(true, "OK", "已取消");
    }

    /** 无活跃运行，幂等成功 */
    public static AguiCancelResponse noActiveRun() {
        return new AguiCancelResponse(false, "NO_ACTIVE_RUN", "该 session 当前无活跃运行");
    }
}
