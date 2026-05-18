package xiaogj.xzagent.repository;

import java.util.List;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;

/**
 * 远程工具元数据源仓储。
 */
public interface RemoteToolSourceRepository {

    /**
     * 查询所有启用中的远程元数据源。
     *
     * @return 启用中的元数据源列表
     */
    List<RemoteToolSourceDefinition> findEnabledSources();

    /**
     * 查询全部元数据源。
     */
    List<RemoteToolSourceDefinition> findAll();

    /**
     * 保存或更新元数据源。
     */
    void upsert(RemoteToolSourceDefinition definition);

    /**
     * 按来源 ID 删除元数据源。
     *
     * @param sourceId 来源唯一标识
     */
    void deleteBySourceId(String sourceId);
}
