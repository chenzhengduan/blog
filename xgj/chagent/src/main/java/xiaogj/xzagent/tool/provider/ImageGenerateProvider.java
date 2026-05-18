package xiaogj.xzagent.tool.provider;

import java.util.List;

/**
 * 图像生成供应商接口。
 *
 * <p>各实现负责调用自身 API 返回临时图片 URL 列表。
 * 下载与持久化上传由上层 {@link xiaogj.xzagent.tool.ImageGenerateTool} 统一处理。
 */
public interface ImageGenerateProvider {

    /**
     * 获取当前供应商名称，用于审计与用量记录。
     *
     * @return 供应商标识，如 dashscope、openai
     */
    String getProviderName();

    /**
     * 获取当前供应商实际使用的模型名称，用于审计与用量记录。
     *
     * @return 模型名称，如 wan2.7-image、dall-e-3
     */
    String getModelName();

    /**
     * 调用供应商 API 生成图片，返回临时图片 URL 列表。
     *
     * @param params 标准化入参
     * @return 临时图片 URL 列表，不为空
     * @throws Exception 生成失败时抛出
     */
    List<String> generateTemporaryUrls(ImageGenerateParams params) throws Exception;
}
