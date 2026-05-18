package xiaogj.xzagent.tool.provider;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * 图像生成工具的标准化入参，供各供应商 Provider 使用。
 *
 * @param prompt         文本提示词
 * @param negativePrompt 反向提示词，null 表示不指定
 * @param uploadUri      图片持久化上传地址
 * @param size           输出分辨率，格式 WxH，如 1024x1024
 * @param n              生图数量
 * @param images         参考图片列表，空列表表示纯文生图
 * @param seed           随机种子，null 表示不指定
 */
public record ImageGenerateParams(
        String prompt,
        String negativePrompt,
        URI uploadUri,
        String size,
        int n,
        List<String> images,
        Integer seed) {

    /** 默认输出分辨率。 */
    public static final String DEFAULT_SIZE = "1024x1024";

    public ImageGenerateParams {
        // 防御性拷贝，避免外部修改
        images = images != null ? List.copyOf(images) : Collections.emptyList();
    }
}
