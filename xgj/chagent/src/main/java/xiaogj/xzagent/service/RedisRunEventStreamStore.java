package xiaogj.xzagent.service;

import io.agentscope.core.agui.encoder.AguiEventEncoder;
import io.agentscope.core.agui.event.AguiEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xiaogj.xzagent.config.XzagentDistributedRunProperties;

/**
 * 基于 Redis Stream 的运行事件流存储。
 *
 * <p>运行中事件仍然完整写入 Redis Stream，便于排障和未来扩展更强的追流能力；
 * 同时额外维护一份按 {@code seq} 建立的轻量索引，避免重连补流时每次都从 Stream
 * 起点全量扫描。
 */
@Service
public class RedisRunEventStreamStore implements RunEventStreamStore {

    /** Stream 字段：事件序号。 */
    private static final String FIELD_SEQ = "seq";
    /** Stream 字段：事件 JSON 负载。 */
    private static final String FIELD_PAYLOAD = "payload";
    /** 序号索引 member 的分隔符。 */
    private static final String INDEX_MEMBER_SEPARATOR = "|";

    /** Redis 字符串模板。 */
    private final StringRedisTemplate stringRedisTemplate;
    /** 分布式运行配置。 */
    private final XzagentDistributedRunProperties properties;
    /** AGUI 事件编码器。 */
    private final AguiEventEncoder aguiEventEncoder = new AguiEventEncoder();

    public RedisRunEventStreamStore(
            StringRedisTemplate stringRedisTemplate,
            XzagentDistributedRunProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.properties = properties;
    }

    @Override
    public StoredRunEvent append(String runId, long seq, AguiEvent event) {
        String payloadJson = aguiEventEncoder.encodeToJson(event).trim();
        RecordId recordId = stringRedisTemplate.opsForStream().add(StreamRecords.string(
                Map.of(
                        FIELD_SEQ, Long.toString(seq),
                        FIELD_PAYLOAD, payloadJson))
                .withStreamKey(streamKey(runId)));
        String recordIdValue = recordId != null ? recordId.getValue() : "seq-" + seq;
        stringRedisTemplate.opsForZSet().add(indexKey(runId), indexMember(seq, recordIdValue), seq);
        stringRedisTemplate.opsForHash().put(payloadKey(runId), Long.toString(seq), payloadJson);
        expire(runId);
        return new StoredRunEvent(recordIdValue, seq, payloadJson);
    }

    @Override
    public List<StoredRunEvent> replayAfter(String runId, long lastEventSeq, int limit) {
        int effectiveLimit = Math.max(limit, 1);
        Set<String> indexMembers = stringRedisTemplate.opsForZSet().rangeByScore(
                indexKey(runId),
                lastEventSeq + 1D,
                Double.POSITIVE_INFINITY,
                0,
                effectiveLimit);
        if (indexMembers == null || indexMembers.isEmpty()) {
            return List.of();
        }

        List<IndexedEventRef> indexedRefs = indexMembers.stream().map(this::parseIndexMember).toList();
        List<Object> payloadLookupKeys = new ArrayList<>(indexedRefs.size());
        for (IndexedEventRef indexedRef : indexedRefs) {
            payloadLookupKeys.add(Long.toString(indexedRef.seq()));
        }
        List<Object> payloads = stringRedisTemplate.opsForHash().multiGet(
                payloadKey(runId),
                payloadLookupKeys);
        if (payloads == null || payloads.isEmpty()) {
            return List.of();
        }

        List<StoredRunEvent> replay = new ArrayList<>();
        for (int i = 0; i < indexedRefs.size(); i++) {
            String payloadJson = i < payloads.size() ? stringValue(payloads.get(i)) : "";
            if (payloadJson.isBlank()) {
                continue;
            }
            IndexedEventRef ref = indexedRefs.get(i);
            replay.add(new StoredRunEvent(ref.recordId(), ref.seq(), payloadJson));
        }
        return replay;
    }

    @Override
    public void expire(String runId) {
        stringRedisTemplate.expire(streamKey(runId), properties.eventStreamTtlDuration());
        stringRedisTemplate.expire(indexKey(runId), properties.eventStreamTtlDuration());
        stringRedisTemplate.expire(payloadKey(runId), properties.eventStreamTtlDuration());
    }

    private String streamKey(String runId) {
        return "xzagent:run:events:" + runId;
    }

    private String indexKey(String runId) {
        return "xzagent:run:event-index:" + runId;
    }

    private String payloadKey(String runId) {
        return "xzagent:run:event-payload:" + runId;
    }

    private String indexMember(long seq, String recordId) {
        return seq + INDEX_MEMBER_SEPARATOR + recordId;
    }

    private IndexedEventRef parseIndexMember(String indexMember) {
        int separatorIndex = indexMember.indexOf(INDEX_MEMBER_SEPARATOR);
        if (separatorIndex < 0) {
            throw new IllegalStateException("非法的运行事件索引 member: " + indexMember);
        }
        long seq = Long.parseLong(indexMember.substring(0, separatorIndex));
        String recordId = indexMember.substring(separatorIndex + 1);
        return new IndexedEventRef(seq, recordId);
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private record IndexedEventRef(long seq, String recordId) {
    }
}
