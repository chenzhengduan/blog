package xiaogj.xzagent.repository.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import xiaogj.xzagent.model.ImageGenerateUsage;
import xiaogj.xzagent.repository.ImageGenerateUsageRepository;

/**
 * {@link ImageGenerateUsageRepository} 的 JdbcTemplate 实现。
 *
 * <p>写入失败仅记录日志，不中断图片生成工具主链路。
 */
@Repository
public class JdbcImageGenerateUsageRepository implements ImageGenerateUsageRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcImageGenerateUsageRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcImageGenerateUsageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ImageGenerateUsage usage) {
        try {
            jdbcTemplate.update(
                    """
                    insert into image_generate_usage
                        (user_id, session_id, run_id, provider, model, size, image_count)
                    values
                        (?, ?, ?, ?, ?, ?, ?)
                    """,
                    usage.userId(),
                    usage.sessionId(),
                    usage.runId(),
                    usage.provider(),
                    usage.model(),
                    usage.size(),
                    usage.imageCount());
        } catch (Exception e) {
            log.error("图片生成用量记录写入失败（不影响主链路）: runId={}, userId={}, error={}",
                    usage.runId(), usage.userId(), e.getMessage(), e);
        }
    }
}
