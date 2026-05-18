package xiaogj.xzagent.tool;

/**
 * HTML 截图工具相关常量。
 *
 * <p>把对外下载路径和输出目录抽成常量，避免工具返回值与控制器路由各写一份，
 * 后续一边改了另一边忘记同步。
 */
public final class HtmlScreenshotConstants {

    /**
     * 截图结果的对外访问路径前缀。
     */
    public static final String PUBLIC_PATH_PREFIX = "/xzagent/api/tools/html-screenshots";

    private HtmlScreenshotConstants() {
    }
}
