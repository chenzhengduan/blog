package xiaogj.xzagent.service;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.util.SkillUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import xiaogj.xzagent.config.SkillRepositoryConfig;
import xiaogj.xzagent.repository.SkillMcpBindingRepository;
import xiaogj.xzagent.repository.SkillRemoteToolBindingRepository;
import xiaogj.xzagent.web.dto.SkillCatalogResponse;
import xiaogj.xzagent.web.dto.SkillDetailResponse;
import xiaogj.xzagent.web.dto.SkillInlineUpsertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/**
 * Skill 管理应用服务。
 *
 * <p>当前版本将 Skill 的唯一来源收敛到数据库，因此所有新增、编辑、删除
 * 操作都统一经过该服务，再由 AgentScope 的 {@link AgentSkillRepository}
 * 持久化到 MySQL。
 */
@Service
public class SkillAdminService {

    private static final Logger log = LoggerFactory.getLogger(SkillAdminService.class);

    /**
     * 持久化到数据库时统一使用固定 source。
     *
     * <p>这样做可以保证运行时 `skillId = name_source` 稳定，不受数据库名、
     * 表名或部署环境变化影响。
     */
    private static final String SKILL_SOURCE = SkillRepositoryConfig.SKILL_SOURCE;

    private final AgentSkillRepository skillRepository;
    private final SkillCatalogService skillCatalogService;
    private final RuntimeSnapshotRegistry runtimeSnapshotRegistry;
    private final SkillMcpBindingRepository skillMcpBindingRepository;
    private final SkillRemoteToolBindingRepository skillRemoteToolBindingRepository;
    private final JdbcTemplate jdbcTemplate;

    public SkillAdminService(
            AgentSkillRepository skillRepository,
            SkillCatalogService skillCatalogService,
            RuntimeSnapshotRegistry runtimeSnapshotRegistry,
            SkillMcpBindingRepository skillMcpBindingRepository,
            SkillRemoteToolBindingRepository skillRemoteToolBindingRepository,
            JdbcTemplate jdbcTemplate) {
        this.skillRepository = skillRepository;
        this.skillCatalogService = skillCatalogService;
        this.runtimeSnapshotRegistry = runtimeSnapshotRegistry;
        this.skillMcpBindingRepository = skillMcpBindingRepository;
        this.skillRemoteToolBindingRepository = skillRemoteToolBindingRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 返回 Skill 目录列表。
     */
    public List<SkillCatalogResponse> listSkills() {
        return skillCatalogService.listSkills();
    }

    /**
     * 查询单个 Skill 详情。
     */
    public SkillDetailResponse getSkill(String skillId) {
        AgentSkill skill = loadSkillOrThrow(skillId);
        return toDetailResponse(skill);
    }

    /**
     * 通过 `SKILL.md + 资源映射` 创建或更新 Skill。
     *
     * <p>这里统一使用 AgentScope 提供的 `SkillUtil` 解析 frontmatter，确保
     * 格式约束与运行时保持一致。
     */
    public SkillDetailResponse saveInline(String pathSkillId, SkillInlineUpsertRequest request) {
        Map<String, String> resources = normalizeResources(request.resources());
        AgentSkill parsedSkill = SkillUtil.createFrom(request.skillMd(), resources, SKILL_SOURCE);
        validateSkillIdentity(pathSkillId, parsedSkill);

        boolean force = Boolean.TRUE.equals(request.force());
        boolean saved = skillRepository.save(List.of(parsedSkill), force);
        if (!saved) {
            throw new IllegalStateException("Skill 已存在，请使用强制覆盖或改用更新接口");
        }
        runtimeSnapshotRegistry.refresh();
        return getSkill(parsedSkill.getName());
    }

    /**
     * 通过 `.skill/.zip` 包上传 Skill。
     *
     * <p>技能包格式直接复用 AgentScope 的 zip 解析规则，避免重复发明格式。
     */
    public Mono<SkillDetailResponse> savePackage(String pathSkillId, FilePart file, boolean force) {
        if (file == null) {
            throw new IllegalArgumentException("Skill 包不能为空");
        }
        return DataBufferUtils.join(file.content())
                .map(dataBuffer -> {
                    try {
                        byte[] fileBytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(fileBytes);
                        if (fileBytes.length == 0) {
                            throw new IllegalArgumentException("Skill 包不能为空");
                        }
                        return fileBytes;
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .map(fileBytes -> savePackageBytes(pathSkillId, fileBytes, force));
    }

    /**
     * 解析上传的压缩包字节并落库。
     *
     * <p>WebFlux 场景下控制器通过 {@link FilePart} 提供文件内容，这里收敛为
     * byte[] 后复用统一的技能包导入逻辑。
     */
    private SkillDetailResponse savePackageBytes(String pathSkillId, byte[] fileBytes, boolean force) {
        byte[] sanitizedBytes = stripPlatformJunkEntries(fileBytes);
        AgentSkill parsedSkill;
        try {
            parsedSkill = SkillUtil.createFromZip(sanitizedBytes, SKILL_SOURCE);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Skill 包结构校验失败: pathSkillId={}, message={}, zipEntries={}",
                    pathSkillId,
                    exception.getMessage(),
                    listZipEntries(sanitizedBytes));
            throw exception;
        }
        validateSkillIdentity(pathSkillId, parsedSkill);
        boolean saved = skillRepository.save(List.of(parsedSkill), force);
        if (!saved) {
            throw new IllegalStateException("Skill 已存在，请使用强制覆盖或改用更新接口");
        }
        runtimeSnapshotRegistry.refresh();
        return getSkill(parsedSkill.getName());
    }

    /**
     * 过滤 macOS/Finder 自动写入的垃圾 entry。
     *
     * <p>这些文件不属于 Skill 资源，但会破坏 AgentScope 对 zip 根目录的一致性校验。
     * 这里只做平台噪音清理，不修改真正的 Skill 目录结构。
     */
    private byte[] stripPlatformJunkEntries(byte[] fileBytes) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(fileBytes));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (isPlatformJunkEntry(entryName)) {
                    continue;
                }
                ZipEntry cleanedEntry = new ZipEntry(entryName);
                cleanedEntry.setTime(entry.getTime());
                zipOutputStream.putNextEntry(cleanedEntry);
                if (!entry.isDirectory()) {
                    copy(zipInputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("清理 Skill 包平台垃圾文件失败", exception);
        }
    }

    /**
     * 判断 zip entry 是否属于平台自动生成的垃圾文件。
     */
    private boolean isPlatformJunkEntry(String entryName) {
        if (!StringUtils.hasText(entryName)) {
            return true;
        }
        String normalized = entryName.replace('\\', '/');
        return normalized.startsWith("__MACOSX/")
                || normalized.equals(".DS_Store")
                || normalized.endsWith("/.DS_Store")
                || normalized.startsWith("._")
                || normalized.contains("/._");
    }

    /**
     * 复制 zip entry 的字节内容。
     */
    private void copy(InputStream inputStream, ZipOutputStream outputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
    }

    /**
     * 枚举 zip 内部 entry 名称，便于定位技能包结构问题。
     *
     * <p>这里只记录 entry 路径，不读取文件内容，也不打印正文，避免日志泄露。
     */
    private List<String> listZipEntries(byte[] fileBytes) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(fileBytes))) {
            List<String> entries = new java.util.ArrayList<>();
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
            return entries;
        } catch (IOException exception) {
            log.warn("读取 Skill 包 entry 列表失败", exception);
            return List.of("<读取失败>");
        }
    }

    /**
     * 删除 Skill。
     *
     * <p>删除前会先清理业务侧 Skill 绑定，避免数据库中残留悬空引用。
     */
    public void deleteSkill(String skillId) {
        cleanupBindings(skillId);
        boolean deleted = skillRepository.delete(skillId);
        if (!deleted) {
            throw new IllegalArgumentException("未找到 Skill: " + skillId);
        }
        runtimeSnapshotRegistry.refresh();
    }

    /**
     * 清理围绕 Skill 名称建立的业务绑定关系。
     */
    private void cleanupBindings(String skillId) {
        skillMcpBindingRepository.replaceBySkillId(skillId, List.of());
        skillRemoteToolBindingRepository.replaceBySkillId(skillId, List.of());
        jdbcTemplate.update("delete from skill_local_tool_binding where skill_id = ?", skillId);
        jdbcTemplate.update("delete from session_active_skill where skill_id = ?", skillId);
    }

    /**
     * 校验路径参数与技能 frontmatter 是否一致。
     *
     * <p>如果前端传了路径变量，就必须与 `SKILL.md` 中的 `name` 保持一致；
     * 否则后续绑定关系会出现“一份内容对应两个 Skill ID”的歧义。
     */
    private void validateSkillIdentity(String pathSkillId, AgentSkill parsedSkill) {
        if (StringUtils.hasText(pathSkillId) && !parsedSkill.getName().equals(pathSkillId)) {
            throw new IllegalArgumentException("路径中的 skillId 与 SKILL.md 中的 name 不一致");
        }
    }

    /**
     * 规整资源映射。
     *
     * <p>这里只保留非空路径和非空内容，防止前端表单产生的空资源占位污染仓库。
     */
    private Map<String, String> normalizeResources(Map<String, String> resources) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (resources == null || resources.isEmpty()) {
            return normalized;
        }
        for (Map.Entry<String, String> entry : resources.entrySet()) {
            if (!StringUtils.hasText(entry.getKey())) {
                continue;
            }
            if (entry.getValue() == null) {
                continue;
            }
            normalized.put(entry.getKey().trim(), entry.getValue());
        }
        return normalized;
    }

    /**
     * 从仓库加载 Skill，不存在则抛出显式异常。
     */
    private AgentSkill loadSkillOrThrow(String skillId) {
        AgentSkill skill = skillRepository.getSkill(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("未找到 Skill: " + skillId);
        }
        return skill;
    }

    /**
     * 转换为详情响应。
     */
    private SkillDetailResponse toDetailResponse(AgentSkill skill) {
        return new SkillDetailResponse(
                skill.getName(),
                skill.getName(),
                skill.getDescription(),
                skill.getSkillContent(),
                skill.getResources(),
                skill.getSource());
    }
}
