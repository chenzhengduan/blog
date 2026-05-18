package xiaogj.xzagent.tool.provider;

/**
 * 图像生成业务异常。
 *
 * <p>已包含适合直接展示给用户的错误信息，外层无需额外拼接上下文。
 */
public class ImageGenerateException extends RuntimeException {

    public ImageGenerateException(String message) {
        super(message);
    }
}
