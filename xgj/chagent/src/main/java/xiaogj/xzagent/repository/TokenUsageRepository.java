package xiaogj.xzagent.repository;

import xiaogj.xzagent.model.TokenUsage;

/**
 * Token 消耗记录的持久化接口。
 *
 * <p>实现类须保证写入失败不向调用方抛出受检异常，避免中断 Agent 主链路。
 */
public interface TokenUsageRepository {

    /**
     * 持久化一条 token 消耗记录。
     *
     * @param tokenUsage 本次 agent.call() 的 token 累计消耗
     */
    void save(TokenUsage tokenUsage);
}
