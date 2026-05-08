package xiaogj.xzagent.repository;

import xiaogj.xzagent.model.ImageGenerateUsage;

/**
 * 图片生成用量记录的持久化接口。
 *
 * <p>实现类应保证写入失败不影响图片生成主链路，调用方无需自行兜底异常。
 */
public interface ImageGenerateUsageRepository {

    /**
     * 持久化一条图片生成用量记录。
     *
     * @param usage 本次图片生成工具调用的用量明细
     */
    void save(ImageGenerateUsage usage);
}
