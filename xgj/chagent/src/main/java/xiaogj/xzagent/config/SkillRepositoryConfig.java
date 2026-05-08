package xiaogj.xzagent.config;

import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.mysql.MysqlSkillRepository;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Skill 仓库配置。
 *
 * <p>当前版本将 Skill 统一收敛到数据库，不再从 classpath 读取内置技能。
 * 这样可以支持前端上传、在线编辑与容器化部署，不再依赖应用镜像中的静态
 * 技能文件。
 */
@Configuration
public class SkillRepositoryConfig {

    /**
     * Skill 在数据库中的来源标识。
     *
     * <p>这里使用稳定常量，而不是由 AgentScope 仓库根据库名/表名自动拼接的
     * 动态来源值。这样可确保运行时 Skill ID 在不同环境中保持稳定，避免因为
     * 数据库名变化导致 `name_source` 后缀变化，进而污染业务侧绑定表。
     */
    public static final String SKILL_SOURCE = "jegent_db";

    /** Skill 主表名称。 */
    public static final String SKILLS_TABLE_NAME = "agent_skill";

    /** Skill 资源表名称。 */
    public static final String RESOURCES_TABLE_NAME = "agent_skill_resource";

    /**
     * 装配基于 MySQL 的 Skill 仓库。
     *
     * <p>这里复用当前业务数据源，并将 Skill 表放到同一个数据库中。数据库名
     * 通过 JDBC 当前连接的 catalog 动态解析，避免在配置文件里重复维护一份
     * 逻辑相同的 schema 名称。
     */
    @Bean
    public AgentSkillRepository skillRepository(DataSource dataSource) {
        return MysqlSkillRepository.builder(dataSource)
                .databaseName(resolveDatabaseName(dataSource))
                .skillsTableName(SKILLS_TABLE_NAME)
                .resourcesTableName(RESOURCES_TABLE_NAME)
                .createIfNotExist(true)
                .writeable(true)
                .build();
    }

    /**
     * 从当前数据源连接中解析数据库名。
     *
     * <p>MySQL 驱动会把 JDBC URL 中的数据库名暴露为 catalog。这里统一从
     * catalog 取值，避免把 schema 名重复硬编码到配置文件和代码中。
     */
    private String resolveDatabaseName(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            if (catalog == null || catalog.isBlank()) {
                throw new IllegalStateException("无法从数据源连接解析当前数据库名");
            }
            return catalog;
        } catch (SQLException exception) {
            throw new IllegalStateException("初始化 Skill 仓库时无法获取数据库名", exception);
        }
    }
}
