package xiaogj.xzagent.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import xiaogj.xzagent.config.XzagentDistributedRunProperties;
import xiaogj.xzagent.model.AgentRunStatus;
import xiaogj.xzagent.model.DistributedRunMetadata;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 基于 Redis 的活跃运行协调实现。
 *
 * <p>本实现将协调层拆成两类键：
 * <ul>
 *     <li>{@code xzagent:session:active:{sessionId}}：保存当前活跃 runId，并承担短 lease</li>
 *     <li>{@code xzagent:run:meta:{runId}}：保存 owner、状态、heartbeat、取消标记等元数据</li>
 * </ul>
 *
 * <p>这样设计的好处是：
 * <ul>
 *     <li>按 session 做互斥抢占足够直接</li>
 *     <li>按 runId 查询 owner / 状态时不需要反查数据库</li>
 *     <li>断线补流与取消转发都能围绕 runId 做定位</li>
 * </ul>
 */
@Service
public class RedisDistributedRunCoordinator implements DistributedRunCoordinator {

    /** Redis 字符串模板。 */
    private final StringRedisTemplate stringRedisTemplate;
    /** 分布式运行配置。 */
    private final XzagentDistributedRunProperties properties;

    public RedisDistributedRunCoordinator(
            StringRedisTemplate stringRedisTemplate,
            XzagentDistributedRunProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.properties = properties;
    }

    @Override
    public boolean tryClaim(String sessionId, String runId, String ownerNodeId, String ownerBaseUrl) {
        Boolean claimed = stringRedisTemplate.opsForValue().setIfAbsent(
                activeSessionKey(sessionId),
                runId,
                properties.activeLeaseDuration());
        if (!Boolean.TRUE.equals(claimed)) {
            return false;
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("sessionId", sessionId);
        metadata.put("runId", runId);
        metadata.put("ownerNodeId", ownerNodeId);
        metadata.put("ownerBaseUrl", ownerBaseUrl);
        metadata.put("status", AgentRunStatus.RUNNING.name());
        metadata.put("lastEventSeq", "0");
        metadata.put("startedAt", now.toString());
        metadata.put("lastHeartbeatAt", now.toString());
        metadata.put("cancelRequested", Boolean.FALSE.toString());
        stringRedisTemplate.opsForHash().putAll(runMetaKey(runId), metadata);
        stringRedisTemplate.expire(runMetaKey(runId), metadataTtlDuration());
        return true;
    }

    @Override
    public Optional<DistributedRunMetadata> findActiveRun(String sessionId) {
        String runId = stringRedisTemplate.opsForValue().get(activeSessionKey(sessionId));
        if (runId == null || runId.isBlank()) {
            return Optional.empty();
        }
        return findRun(runId).filter(metadata -> sessionId.equals(metadata.sessionId()));
    }

    @Override
    public Optional<DistributedRunMetadata> findRun(String runId) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(runMetaKey(runId));
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toMetadata(entries));
    }

    @Override
    public boolean heartbeat(String runId, long lastEventSeq) {
        Optional<DistributedRunMetadata> metadataOptional = findRun(runId);
        if (metadataOptional.isEmpty()) {
            return false;
        }
        DistributedRunMetadata metadata = metadataOptional.get();
        String activeRunId = stringRedisTemplate.opsForValue().get(activeSessionKey(metadata.sessionId()));
        if (!runId.equals(activeRunId)) {
            return false;
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        stringRedisTemplate.opsForHash().put(runMetaKey(runId), "lastHeartbeatAt", now.toString());
        stringRedisTemplate.opsForHash().put(runMetaKey(runId), "lastEventSeq", Long.toString(lastEventSeq));
        stringRedisTemplate.expire(activeSessionKey(metadata.sessionId()), properties.activeLeaseDuration());
        stringRedisTemplate.expire(runMetaKey(runId), metadataTtlDuration());
        return true;
    }

    @Override
    public void updateLastEventSeq(String runId, long lastEventSeq) {
        stringRedisTemplate.opsForHash().put(runMetaKey(runId), "lastEventSeq", Long.toString(lastEventSeq));
        stringRedisTemplate.expire(runMetaKey(runId), metadataTtlDuration());
    }

    @Override
    public void updateStatus(String runId, AgentRunStatus status) {
        stringRedisTemplate.opsForHash().put(runMetaKey(runId), "status", status.name());
        stringRedisTemplate.expire(runMetaKey(runId), metadataTtlDuration());
    }

    @Override
    public void requestCancel(String runId) {
        stringRedisTemplate.opsForHash().put(runMetaKey(runId), "cancelRequested", Boolean.TRUE.toString());
        stringRedisTemplate.expire(runMetaKey(runId), metadataTtlDuration());
    }

    @Override
    public void clear(String sessionId, String runId) {
        String activeRunId = stringRedisTemplate.opsForValue().get(activeSessionKey(sessionId));
        if (runId.equals(activeRunId)) {
            stringRedisTemplate.delete(activeSessionKey(sessionId));
        }
        stringRedisTemplate.expire(runMetaKey(runId), metadataTtlDuration());
    }

    private DistributedRunMetadata toMetadata(Map<Object, Object> entries) {
        return new DistributedRunMetadata(
                stringValue(entries, "sessionId"),
                stringValue(entries, "runId"),
                stringValue(entries, "ownerNodeId"),
                stringValue(entries, "ownerBaseUrl"),
                AgentRunStatus.valueOf(stringValue(entries, "status")),
                longValue(entries, "lastEventSeq"),
                instantValue(entries, "startedAt"),
                instantValue(entries, "lastHeartbeatAt"),
                booleanValue(entries, "cancelRequested"));
    }

    private String activeSessionKey(String sessionId) {
        return "xzagent:session:active:" + sessionId;
    }

    private String runMetaKey(String runId) {
        return "xzagent:run:meta:" + runId;
    }

    private java.time.Duration metadataTtlDuration() {
        return java.time.Duration.ofSeconds(Math.max(properties.activeLeaseSeconds() * 2L, 2L));
    }

    private String stringValue(Map<Object, Object> entries, String key) {
        Object value = entries.get(key);
        return value == null ? "" : value.toString();
    }

    private long longValue(Map<Object, Object> entries, String key) {
        String value = stringValue(entries, key);
        return value.isBlank() ? 0L : Long.parseLong(value);
    }

    private Instant instantValue(Map<Object, Object> entries, String key) {
        String value = stringValue(entries, key);
        return value.isBlank() ? Instant.EPOCH : Instant.parse(value);
    }

    private boolean booleanValue(Map<Object, Object> entries, String key) {
        return Boolean.parseBoolean(stringValue(entries, key));
    }
}
