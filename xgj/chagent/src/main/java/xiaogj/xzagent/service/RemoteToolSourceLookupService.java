package xiaogj.xzagent.service;

import java.util.LinkedHashMap;
import java.util.Map;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import org.springframework.stereotype.Service;

/**
 * 远程工具元数据源查询服务。
 *
 * <p>该服务把仓储结果转成以 `sourceId` 为键的只读视图，便于后续
 * 运行时组件快速按源定位元数据配置。
 */
@Service
public class RemoteToolSourceLookupService {

    private final RemoteToolSourceRepository remoteToolSourceRepository;

    public RemoteToolSourceLookupService(RemoteToolSourceRepository remoteToolSourceRepository) {
        this.remoteToolSourceRepository = remoteToolSourceRepository;
    }

    /**
     * 查询所有启用中的 source，并按 sourceId 建立索引。
     */
    public Map<String, RemoteToolSourceDefinition> getEnabledSourcesById() {
        Map<String, RemoteToolSourceDefinition> sources = new LinkedHashMap<>();
        for (RemoteToolSourceDefinition definition : remoteToolSourceRepository.findEnabledSources()) {
            sources.put(definition.sourceId(), definition);
        }
        return sources;
    }
}
