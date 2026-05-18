package xiaogj.xzagent.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 基于内存的活跃 Agent 运行注册表。
 *
 * <p>使用 {@link ConcurrentHashMap} 存储 sessionId → {@link RunStreamRegistry.ActiveRun} 的映射，
 * 线程安全，适合单节点部署。
 *
 * <p><b>多节点注意：</b>此实现本身仍不跨节点共享，但在分布式部署下依然保留价值：
 * owner 节点需要通过它保存本地 {@link AgentRuntime} 和 live 热流，以支持同节点在线订阅、
 * 中断执行与最终收口。
 */
@Component
public class InMemoryRunStreamRegistry implements RunStreamRegistry {

    private static final Logger log = LoggerFactory.getLogger(InMemoryRunStreamRegistry.class);

    /** sessionId → 活跃运行上下文的映射表 */
    private final ConcurrentHashMap<String, ActiveRun> registry = new ConcurrentHashMap<>();

    @Override
    public void register(String sessionId, ActiveRun activeRun) {
        registry.put(sessionId, activeRun);
        log.debug("注册活跃运行: sessionId={}, runId={}, 当前活跃数={}",
                sessionId, activeRun.runtime().runId(), registry.size());
    }

    @Override
    public Optional<ActiveRun> find(String sessionId) {
        return Optional.ofNullable(registry.get(sessionId));
    }

    @Override
    public void remove(String sessionId) {
        ActiveRun removed = registry.remove(sessionId);
        if (removed != null) {
            log.debug("移除活跃运行: sessionId={}, runId={}, 当前活跃数={}",
                    sessionId, removed.runtime().runId(), registry.size());
        }
    }
}
