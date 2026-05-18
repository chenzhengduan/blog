package xiaogj.xzagent.service;

import io.agentscope.core.agui.event.AguiEvent;
import java.util.List;

/**
 * 运行事件流存储。
 *
 * <p>该抽象面向“断线补流”场景：owner 节点在产生 AGUI 事件时先落一份可回放副本，
 * 重连节点则按事件序号取回缺失片段，再继续接收后续事件。
 *
 * <p>第一版接口只暴露最小能力：追加事件、按序号补流、设置过期时间。
 * live 轮询与跨节点订阅链路由上层控制器/协调器继续编排，避免把控制面逻辑硬塞进存储层。
 */
public interface RunEventStreamStore {

    /**
     * 已持久化的运行事件。
     *
     * @param streamRecordId Redis Stream 记录 ID，便于后续排障和扩展为按记录游标追流
     * @param seq           运行内单调递增事件序号
     * @param payloadJson   已编码的 AGUI 事件 JSON，可直接写入 SSE data
     */
    record StoredRunEvent(String streamRecordId, long seq, String payloadJson) {
    }

    /**
     * 追加一个事件到运行事件流。
     */
    StoredRunEvent append(String runId, long seq, AguiEvent event);

    /**
     * 回放指定序号之后的事件。
     */
    List<StoredRunEvent> replayAfter(String runId, long lastEventSeq, int limit);

    /**
     * 为运行事件流设置过期时间，供 run 收口后异步回收。
     */
    void expire(String runId);
}
