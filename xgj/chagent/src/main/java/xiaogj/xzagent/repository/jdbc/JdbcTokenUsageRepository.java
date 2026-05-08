package xiaogj.xzagent.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import xiaogj.xzagent.model.TokenUsage;
import xiaogj.xzagent.repository.TokenUsageRepository;

/**
 * {@link TokenUsageRepository} 的 JdbcTemplate 实现。
 *
 * <p>写入失败仅记录日志，不向调用方抛出异常，以保证 Agent 主链路不受影响。
 */
@Repository
public class JdbcTokenUsageRepository implements TokenUsageRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcTokenUsageRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcTokenUsageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(TokenUsage tokenUsage) {
        try {
            jdbcTemplate.update(
                    """
                    insert into token_usage
                        (user_id, session_id, run_id, agent_id, model_provider, model_name, input_tokens, output_tokens)
                    values
                        (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    tokenUsage.userId(),
                    tokenUsage.sessionId(),
                    tokenUsage.runId(),
                    tokenUsage.agentId(),
                    tokenUsage.modelProvider(),
                    tokenUsage.modelName(),
                    tokenUsage.inputTokens(),
                    tokenUsage.outputTokens());
        } catch (Exception e) {
            // Token 记录写入失败不中断 Agent 主链路，仅记录日志供排查
            log.error("token 消耗记录写入失败（不影响 Agent 运行）: runId={}, userId={}, error={}",
                    tokenUsage.runId(), tokenUsage.userId(), e.getMessage(), e);
        }
    }
}
