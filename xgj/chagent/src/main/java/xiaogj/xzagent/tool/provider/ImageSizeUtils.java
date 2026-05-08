package xiaogj.xzagent.tool.provider;

/**
 * 图像尺寸工具方法，供各供应商 Provider 共用。
 */
class ImageSizeUtils {

    /** DashScope 要求的最小总像素数（768x768）。 */
    static final long DASHSCOPE_MIN_PIXELS = 589_824L;

    /** DashScope 要求的最大总像素数（4096x4096）。 */
    static final long DASHSCOPE_MAX_PIXELS = 16_777_216L;

    private ImageSizeUtils() {}

    /**
     * 解析 WxH 格式的尺寸字符串，返回 int[]{width, height}。
     *
     * @throws IllegalArgumentException 格式不正确时
     */
    static int[] parseSize(String size) {
        if (size == null || size.isBlank()) {
            throw new IllegalArgumentException("size 不能为空");
        }
        String[] parts = size.toLowerCase().split("x");
        if (parts.length != 2) {
            throw new IllegalArgumentException("size 格式不正确，应为 WxH，如 1024x1024");
        }
        try {
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("size 宽高必须为正整数");
            }
            return new int[]{width, height};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("size 格式不正确，应为 WxH，如 1024x1024");
        }
    }

    /**
     * 在 prompt 末尾追加宽高比提示，帮助模型按预期比例生成图片。
     *
     * <p>例如 1280x720 会追加 "，图片宽高比为 16:9"。
     */
    static String appendRatioHint(String prompt, int width, int height) {
        int g = gcd(width, height);
        String ratio = (width / g) + ":" + (height / g);
        return prompt + "，图片宽高比为 " + ratio;
    }

    /**
     * 将尺寸等比缩放，使总像素落在 DashScope 要求的范围内。
     *
     * <p>若总像素已在范围内，直接返回原值；否则按比例缩放到边界。
     *
     * @return int[]{adjustedWidth, adjustedHeight}
     */
    static int[] adjustForDashScope(int width, int height) {
        long pixels = (long) width * height;
        if (pixels >= DASHSCOPE_MIN_PIXELS && pixels <= DASHSCOPE_MAX_PIXELS) {
            return new int[]{width, height};
        }
        long target = pixels < DASHSCOPE_MIN_PIXELS ? DASHSCOPE_MIN_PIXELS : DASHSCOPE_MAX_PIXELS;
        double scale = Math.sqrt((double) target / pixels);
        int newWidth = (int) Math.round(width * scale);
        int newHeight = (int) Math.round(height * scale);
        // 防止舍入误差导致仍越界
        while ((long) newWidth * newHeight < DASHSCOPE_MIN_PIXELS) {
            newWidth++;
        }
        while ((long) newWidth * newHeight > DASHSCOPE_MAX_PIXELS) {
            newWidth--;
        }
        return new int[]{newWidth, newHeight};
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
