package xiaogj.xzagent.model;

import io.agentscope.core.state.State;
import java.util.List;
import java.util.Map;

/**
 * A2UI Surface 的当前状态快照，用于会话恢复。
 *
 * @param surfaceId Surface 唯一标识
 * @param catalogId 组件目录标识
 * @param components 最新组件树（全量）
 * @param dataModel 当前数据模型（浅合并累积结果）
 * @param lastUpdatedMsgId 最近一次绑定到的 assistant 消息 ID
 */
public record A2uiSurfaceState(
        String surfaceId,
        String catalogId,
        List<Object> components,
        Map<String, Object> dataModel,
        String lastUpdatedMsgId) implements State {
}
