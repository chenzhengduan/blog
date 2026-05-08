package xiaogj.xzagent.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.util.SkillUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import xiaogj.xzagent.config.SkillRepositoryConfig;
import xiaogj.xzagent.model.McpServerDefinition;
import xiaogj.xzagent.model.RemoteToolSourceDefinition;
import xiaogj.xzagent.model.SkillLocalToolBinding;
import xiaogj.xzagent.model.SkillMcpBinding;
import xiaogj.xzagent.model.SkillRemoteToolBinding;
import xiaogj.xzagent.model.remote.RemoteToolDefinition;
import xiaogj.xzagent.model.remote.RemoteToolMetaDocument;
import xiaogj.xzagent.repository.McpServerRepository;
import xiaogj.xzagent.repository.RemoteToolSourceRepository;
import xiaogj.xzagent.repository.SkillLocalToolBindingRepository;
import xiaogj.xzagent.repository.SkillMcpBindingRepository;
import xiaogj.xzagent.repository.SkillRemoteToolBindingRepository;
import xiaogj.xzagent.web.dto.BundleImportIssueResponse;
import xiaogj.xzagent.web.dto.BundleImportResourceResultResponse;
import xiaogj.xzagent.web.dto.BundleImportResponse;
import xiaogj.xzagent.web.dto.InlineRemoteToolSourceRequest;
import xiaogj.xzagent.web.dto.McpServerRequest;
import xiaogj.xzagent.web.dto.RemoteToolSourceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/**
 * 工程化 bundle 导入服务。
 *
 * <p>第一版 bundle 的目标不是全回滚事务导入，而是：
 * <ul>
 *     <li>按固定阶段顺序导入</li>
 *     <li>每个阶段按资源逐个处理</li>
 *     <li>任一资源失败时停止当前阶段和后续阶段</li>
 *     <li>返回结构化导入结果报告，便于前端展示</li>
 * </ul>
 *
 * <p>这样可以先把“前端一次上传 -> 后端一整套能力导入”跑通，而不在第一版就引入
 * 复杂的跨资源补偿事务。
 */
@Service
public class BundleImportService {

    private static final Logger log = LoggerFactory.getLogger(BundleImportService.class);
    private static final String BUNDLE_ROOT = "bundle/";
    private static final String SKILL_SOURCE = SkillRepositoryConfig.SKILL_SOURCE;

    private final ObjectMapper objectMapper;
    private final AgentSkillRepository skillRepository;
    private final SkillCatalogService skillCatalogService;
    private final RemoteToolAdminService remoteToolAdminService;
    private final McpAdminService mcpAdminService;
    private final RemoteToolSourceRepository remoteToolSourceRepository;
    private final RemoteToolMetaService remoteToolMetaService;
    private final McpServerRepository mcpServerRepository;
    private final SkillLocalToolBindingRepository skillLocalToolBindingRepository;
    private final SkillMcpBindingRepository skillMcpBindingRepository;
    private final SkillRemoteToolBindingRepository skillRemoteToolBindingRepository;
    private final LocalToolCatalogService localToolCatalogService;
    private final AgentDefinitionService agentDefinitionService;
    private final RuntimeSnapshotRegistry runtimeSnapshotRegistry;

    public BundleImportService(
            ObjectMapper objectMapper,
            AgentSkillRepository skillRepository,
            SkillCatalogService skillCatalogService,
            RemoteToolAdminService remoteToolAdminService,
            McpAdminService mcpAdminService,
            RemoteToolSourceRepository remoteToolSourceRepository,
            RemoteToolMetaService remoteToolMetaService,
            McpServerRepository mcpServerRepository,
            SkillLocalToolBindingRepository skillLocalToolBindingRepository,
            SkillMcpBindingRepository skillMcpBindingRepository,
            SkillRemoteToolBindingRepository skillRemoteToolBindingRepository,
            LocalToolCatalogService localToolCatalogService,
            AgentDefinitionService agentDefinitionService,
            RuntimeSnapshotRegistry runtimeSnapshotRegistry) {
        this.objectMapper = objectMapper;
        this.skillRepository = skillRepository;
        this.skillCatalogService = skillCatalogService;
        this.remoteToolAdminService = remoteToolAdminService;
        this.mcpAdminService = mcpAdminService;
        this.remoteToolSourceRepository = remoteToolSourceRepository;
        this.remoteToolMetaService = remoteToolMetaService;
        this.mcpServerRepository = mcpServerRepository;
        this.skillLocalToolBindingRepository = skillLocalToolBindingRepository;
        this.skillMcpBindingRepository = skillMcpBindingRepository;
        this.skillRemoteToolBindingRepository = skillRemoteToolBindingRepository;
        this.localToolCatalogService = localToolCatalogService;
        this.agentDefinitionService = agentDefinitionService;
        this.runtimeSnapshotRegistry = runtimeSnapshotRegistry;
    }

    /**
     * 导入 bundle 压缩包。
     */
    public Mono<BundleImportResponse> importBundle(FilePart filePart, boolean force) {
        if (filePart == null) {
            throw new IllegalArgumentException("Bundle 包不能为空");
        }
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        if (bytes.length == 0) {
                            throw new IllegalArgumentException("Bundle 包不能为空");
                        }
                        return bytes;
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .map(bytes -> importBundleBytes(bytes, force));
    }

    private BundleImportResponse importBundleBytes(byte[] zipBytes, boolean force) {
        ImportCollector collector = new ImportCollector();
        try {
            Map<String, byte[]> entries = readEntries(stripPlatformJunkEntries(zipBytes));
            BundleManifest manifest = parseManifest(entries);
            collector.bundleId = manifest.bundleId();
            collector.bundleType = manifest.bundleType();

            validateManifest(entries, manifest);

            List<ImportedSkill> importedSkills = importSkills(entries, manifest, force, collector);
            collector.completeStage("skills");

            if (shouldImportEnvironment(manifest)) {
                importRemoteSources(entries, force, collector);
                collector.completeStage("remote_sources");
                importMcpServers(entries, force, collector);
                collector.completeStage("mcp_servers");
            }

            importSkillBindings(entries, importedSkills, collector);
            collector.completeStage("bindings");

            importAgentsMd(entries, collector);
        } catch (Exception error) {
            log.error("Bundle 导入失败", error);
            collector.fail("bootstrap", "bundle", "bundle.zip", error.getMessage(), true);
        }
        runtimeSnapshotRegistry.refresh();
        return collector.toResponse();
    }

    private void importAgentsMd(Map<String, byte[]> entries, ImportCollector collector) {
        String agentsMdPath = BUNDLE_ROOT + "AGENTS.md";
        if (!entries.containsKey(agentsMdPath)) {
            return;
        }
        try {
            String content = asUtf8(entries.get(agentsMdPath));
            agentDefinitionService.updateAgentsMd(content);
            collector.applied("agentsMd");
            collector.resource("agent_config", "agents_md", "AGENTS.md", "APPLIED", "AGENTS.md 已导入");
            collector.completeStage("agent_config");
        } catch (Exception error) {
            collector.fail("agent_config", "agents_md", "AGENTS.md", error.getMessage(), false);
        }
    }

    private void validateManifest(Map<String, byte[]> entries, BundleManifest manifest) {
        if (!"1.0".equals(manifest.schemaVersion())) {
            throw new IllegalArgumentException("仅支持 schemaVersion=1.0");
        }
        if (!StringUtils.hasText(manifest.bundleType())) {
            throw new IllegalArgumentException("bundleType 不能为空");
        }
        if (!StringUtils.hasText(manifest.bundleId())) {
            throw new IllegalArgumentException("bundleId 不能为空");
        }
        if ("content-only".equals(manifest.bundleType()) && manifest.shouldImportEnvironment()) {
            throw new IllegalArgumentException("content-only bundle 不允许 importEnvironment=true");
        }
        if (shouldImportEnvironment(manifest) && !hasEnvironmentDirectory(entries)) {
            throw new IllegalArgumentException(
                    "content-with-environment 且 importEnvironment=true 时必须存在 environments/default/");
        }
    }

    private List<ImportedSkill> importSkills(
            Map<String, byte[]> entries,
            BundleManifest manifest,
            boolean force,
            ImportCollector collector) {
        Map<String, Map<String, byte[]>> skills = groupDirectories(entries, BUNDLE_ROOT + "skills/");
        if (skills.isEmpty()) {
            throw new IllegalArgumentException("bundle/skills/ 不能为空");
        }

        List<ImportedSkill> importedSkills = new ArrayList<>();
        for (Map.Entry<String, Map<String, byte[]>> skillEntry : skills.entrySet()) {
            String skillDir = skillEntry.getKey();
            String skillDirName = lastSegment(skillDir);
            try {
                AgentSkill skill = parseSkill(skillDirName, skillEntry.getValue());
                if (!force && skillExists(skill.getName())) {
                    throw new IllegalStateException("Skill 已存在: " + skill.getName());
                }
                boolean saved = skillRepository.save(List.of(skill), force);
                if (!saved) {
                    throw new IllegalStateException("Skill 导入失败: " + skill.getName());
                }
                collector.applied("skills");
                collector.resource("skills", "skill", skill.getName(), "APPLIED", "Skill 已导入");
                importedSkills.add(new ImportedSkill(skill.getName(), skillDir));
            } catch (Exception error) {
                collector.fail("skills", "skill", skillDirName, error.getMessage(), true);
                throw error;
            }
        }
        return importedSkills;
    }

    private AgentSkill parseSkill(String skillDirName, Map<String, byte[]> files) {
        String skillMdPath = files.keySet().stream()
                .filter(path -> path.endsWith("/SKILL.md"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Skill 缺少 SKILL.md: " + skillDirName));
        String skillMd = asUtf8(files.get(skillMdPath));

        Map<String, String> resources = new LinkedHashMap<>();
        String skillRootPrefix = BUNDLE_ROOT + "skills/" + skillDirName + "/";
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String path = entry.getKey();
            if (!path.startsWith(skillRootPrefix)) {
                continue;
            }
            if (path.equals(skillMdPath)) {
                continue;
            }
            String relativePath = path.substring(skillRootPrefix.length());
            if (relativePath.isBlank() || relativePath.endsWith("/")) {
                continue;
            }
            if (relativePath.startsWith("tools/")) {
                continue;
            }
            // 这里要对齐 AgentScope 单 Skill zip 导入的行为：保留 skill 根目录下的
            // 相对路径，而不是只支持 `resources/`。这样 skill 可以自由引用
            // `resources/...`、`references/...` 或其他子目录中的文件。
            resources.put(relativePath, asUtf8(entry.getValue()));
        }
        return SkillUtil.createFrom(skillMd, resources, SKILL_SOURCE);
    }

    private void importRemoteSources(
            Map<String, byte[]> entries,
            boolean force,
            ImportCollector collector) {
        Map<String, Map<String, byte[]>> sources = groupDirectories(
                entries,
                BUNDLE_ROOT + "environments/default/remote_sources/");
        for (Map.Entry<String, Map<String, byte[]>> sourceEntry : sources.entrySet()) {
            String sourceDirName = lastSegment(sourceEntry.getKey());
            try {
                RemoteSourceFile sourceFile = readJsonFromDirectory(
                        sourceEntry.getValue(),
                        "source.json",
                        new TypeReference<>() {});
                if (!force && remoteSourceExists(sourceFile.sourceId())) {
                    throw new IllegalStateException("远程来源已存在: " + sourceFile.sourceId());
                }
                if ("inline".equals(sourceFile.mode())) {
                    String metaContent = asUtf8(findFile(sourceEntry.getValue(), "meta.json"));
                    remoteToolAdminService.saveInlineSource(new InlineRemoteToolSourceRequest(
                            sourceFile.sourceId(),
                            sourceFile.name(),
                            metaContent,
                            sourceFile.enabled(),
                            sourceFile.timeoutSeconds(),
                            sourceFile.refreshIntervalSeconds()));
                } else {
                    if (!StringUtils.hasText(sourceFile.metaUrl())) {
                        throw new IllegalArgumentException("URL 远程来源缺少 metaUrl");
                    }
                    remoteToolAdminService.saveUrlSource(new RemoteToolSourceRequest(
                            sourceFile.sourceId(),
                            sourceFile.name(),
                            sourceFile.metaUrl(),
                            sourceFile.enabled(),
                            Map.of(),
                            sourceFile.timeoutSeconds(),
                            sourceFile.refreshIntervalSeconds()));
                }
                collector.applied("remoteSources");
                collector.resource(
                        "remote_sources",
                        "remote_source",
                        sourceFile.sourceId(),
                        "APPLIED",
                        "远程来源已导入");
            } catch (Exception error) {
                collector.fail("remote_sources", "remote_source", sourceDirName, error.getMessage(), true);
                throw error;
            }
        }
    }

    private void importMcpServers(
            Map<String, byte[]> entries,
            boolean force,
            ImportCollector collector) {
        Map<String, Map<String, byte[]>> servers = groupDirectories(
                entries,
                BUNDLE_ROOT + "environments/default/mcp_servers/");
        for (Map.Entry<String, Map<String, byte[]>> serverEntry : servers.entrySet()) {
            String serverDirName = lastSegment(serverEntry.getKey());
            try {
                McpServerFile serverFile = readJsonFromDirectory(
                        serverEntry.getValue(),
                        "server.json",
                        new TypeReference<>() {});
                if (!force && mcpServerExists(serverFile.serverId())) {
                    throw new IllegalStateException("MCP 服务已存在: " + serverFile.serverId());
                }
                mcpAdminService.saveServer(new McpServerRequest(
                        serverFile.serverId(),
                        serverFile.name(),
                        serverFile.transportType(),
                        serverFile.enabled(),
                        serverFile.config() != null ? serverFile.config() : Map.of(),
                        serverFile.headers() != null ? serverFile.headers() : Map.of(),
                        serverFile.queryParams() != null ? serverFile.queryParams() : Map.of(),
                        serverFile.timeoutSeconds(),
                        serverFile.initializationTimeoutSeconds(),
                        List.of()));
                collector.applied("mcpServers");
                collector.resource("mcp_servers", "mcp_server", serverFile.serverId(), "APPLIED", "MCP 已导入");
            } catch (Exception error) {
                collector.fail("mcp_servers", "mcp_server", serverDirName, error.getMessage(), true);
                throw error;
            }
        }
    }

    private void importSkillBindings(
            Map<String, byte[]> entries,
            List<ImportedSkill> importedSkills,
            ImportCollector collector) {
        for (ImportedSkill skill : importedSkills) {
            try {
                List<LocalToolBindingFile> localBindings = parseLocalBindings(entries, skill.skillDir());
                List<RemoteSourceBindingFile> remoteBindings = parseRemoteBindings(entries, skill.skillDir());
                List<McpServerBindingFile> mcpBindings = parseMcpBindings(entries, skill.skillDir());

                validateLocalTools(localBindings);
                validateRemoteSources(remoteBindings);
                validateMcpServers(mcpBindings);

                skillLocalToolBindingRepository.replaceBySkillId(
                        skill.skillId(),
                        localBindings.stream()
                                .map(binding -> new SkillLocalToolBinding(
                                        skill.skillId(),
                                        binding.toolName(),
                                        binding.enabled()))
                                .toList());

                skillMcpBindingRepository.replaceBySkillId(
                        skill.skillId(),
                        mcpBindings.stream()
                                .map(binding -> new SkillMcpBinding(
                                        skill.skillId(),
                                        binding.serverId(),
                                        binding.enableTools() != null ? binding.enableTools() : List.of(),
                                        binding.disableTools() != null ? binding.disableTools() : List.of(),
                                        binding.enabled()))
                                .toList());

                skillRemoteToolBindingRepository.replaceBySkillId(
                        skill.skillId(),
                        buildRemoteToolBindings(skill.skillId(), remoteBindings));

                collector.applied("localBindings", localBindings.size());
                collector.applied("mcpBindings", mcpBindings.size());
                collector.applied("remoteBindings", remoteBindings.size());
                collector.resource("bindings", "skill", skill.skillId(), "APPLIED", "Skill 绑定已写入");
            } catch (Exception error) {
                collector.fail("bindings", "skill", skill.skillId(), error.getMessage(), true);
                throw error;
            }
        }
    }

    private List<SkillRemoteToolBinding> buildRemoteToolBindings(
            String skillId,
            List<RemoteSourceBindingFile> bindings) {
        List<SkillRemoteToolBinding> result = new ArrayList<>();
        for (RemoteSourceBindingFile binding : bindings) {
            RemoteToolSourceDefinition source = remoteToolSourceRepository.findAll().stream()
                    .filter(item -> item.sourceId().equals(binding.sourceId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("未找到远程来源: " + binding.sourceId()));
            RemoteToolMetaDocument document = remoteToolMetaService.loadMetaDocument(source);
            if (document.tools() == null) {
                continue;
            }
            for (RemoteToolDefinition tool : document.tools()) {
                if (tool == null || !StringUtils.hasText(tool.name())) {
                    continue;
                }
                if (tool.enabled() != null && !tool.enabled()) {
                    continue;
                }
                result.add(new SkillRemoteToolBinding(
                        skillId,
                        binding.sourceId(),
                        tool.name(),
                        binding.enabled()));
            }
        }
        return result;
    }

    private List<LocalToolBindingFile> parseLocalBindings(Map<String, byte[]> entries, String skillDir) {
        String path = skillDir + "/tools/local-tools.json";
        if (!entries.containsKey(path)) {
            return List.of();
        }
        LocalToolsFile file = readJson(entries, path, new TypeReference<>() {});
        return file.tools() != null ? file.tools() : List.of();
    }

    private List<RemoteSourceBindingFile> parseRemoteBindings(Map<String, byte[]> entries, String skillDir) {
        String path = skillDir + "/tools/remote-sources.json";
        if (!entries.containsKey(path)) {
            return List.of();
        }
        RemoteSourcesFile file = readJson(entries, path, new TypeReference<>() {});
        return file.sources() != null ? file.sources() : List.of();
    }

    private List<McpServerBindingFile> parseMcpBindings(Map<String, byte[]> entries, String skillDir) {
        String path = skillDir + "/tools/mcp-servers.json";
        if (!entries.containsKey(path)) {
            return List.of();
        }
        McpServersFile file = readJson(entries, path, new TypeReference<>() {});
        return file.servers() != null ? file.servers() : List.of();
    }

    private void validateLocalTools(List<LocalToolBindingFile> bindings) {
        Set<String> availableTools = localToolCatalogService.listTools().stream()
                .map(item -> item.name())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        for (LocalToolBindingFile binding : bindings) {
            if (!availableTools.contains(binding.toolName())) {
                throw new IllegalArgumentException("未找到本地工具: " + binding.toolName());
            }
        }
    }

    private void validateRemoteSources(List<RemoteSourceBindingFile> bindings) {
        Set<String> availableSourceIds = remoteToolSourceRepository.findAll().stream()
                .map(item -> item.sourceId())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        for (RemoteSourceBindingFile binding : bindings) {
            if (!availableSourceIds.contains(binding.sourceId())) {
                throw new IllegalArgumentException("未找到远程来源: " + binding.sourceId());
            }
        }
    }

    private void validateMcpServers(List<McpServerBindingFile> bindings) {
        Set<String> availableServerIds = mcpServerRepository.findAll().stream()
                .map(McpServerDefinition::serverId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        for (McpServerBindingFile binding : bindings) {
            if (!availableServerIds.contains(binding.serverId())) {
                throw new IllegalArgumentException("未找到 MCP 服务: " + binding.serverId());
            }
        }
    }

    private boolean shouldImportEnvironment(BundleManifest manifest) {
        return "content-with-environment".equals(manifest.bundleType())
                && manifest.shouldImportEnvironment();
    }

    private boolean skillExists(String skillId) {
        return skillCatalogService.listSkills().stream().anyMatch(item -> item.skillId().equals(skillId));
    }

    private boolean remoteSourceExists(String sourceId) {
        return remoteToolSourceRepository.findAll().stream().anyMatch(item -> item.sourceId().equals(sourceId));
    }

    private boolean mcpServerExists(String serverId) {
        return mcpServerRepository.findAll().stream().anyMatch(item -> item.serverId().equals(serverId));
    }

    private BundleManifest parseManifest(Map<String, byte[]> entries) {
        try {
            return objectMapper.readValue(findFile(entries, BUNDLE_ROOT + "bundle.json"), BundleManifest.class);
        } catch (IOException error) {
            throw new IllegalArgumentException("无法解析 bundle.json", error);
        }
    }

    private <T> T readJson(Map<String, byte[]> entries, String path, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(findFile(entries, path), typeReference);
        } catch (IOException error) {
            throw new IllegalArgumentException("无法解析 JSON: " + path, error);
        }
    }

    private <T> T readJsonFromDirectory(
            Map<String, byte[]> directoryFiles,
            String fileName,
            TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(findFile(directoryFiles, fileName), typeReference);
        } catch (IOException error) {
            throw new IllegalArgumentException("无法解析 JSON: " + fileName, error);
        }
    }

    private byte[] findFile(Map<String, byte[]> entries, String pathSuffix) {
        if (entries.containsKey(pathSuffix)) {
            return entries.get(pathSuffix);
        }
        return entries.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(pathSuffix))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("缺少文件: " + pathSuffix));
    }

    private Map<String, byte[]> readEntries(byte[] zipBytes) {
        Map<String, byte[]> entries = new TreeMap<>();
        try (ZipInputStream inputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = normalize(entry.getName());
                if (!entryName.startsWith(BUNDLE_ROOT)) {
                    continue;
                }
                entries.put(entryName, readCurrentEntry(inputStream));
            }
        } catch (IOException error) {
            throw new IllegalStateException("读取 bundle 压缩包失败", error);
        }
        return entries;
    }

    private byte[] readCurrentEntry(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private byte[] stripPlatformJunkEntries(byte[] zipBytes) {
        try (ZipInputStream inputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                java.util.zip.ZipOutputStream zipOutputStream = new java.util.zip.ZipOutputStream(outputStream)) {
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                String entryName = normalize(entry.getName());
                if (isPlatformJunk(entryName)) {
                    continue;
                }
                ZipEntry cleanedEntry = new ZipEntry(entryName);
                cleanedEntry.setTime(entry.getTime());
                zipOutputStream.putNextEntry(cleanedEntry);
                if (!entry.isDirectory()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, read);
                    }
                }
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return outputStream.toByteArray();
        } catch (IOException error) {
            throw new IllegalStateException("清理 bundle 包平台垃圾文件失败", error);
        }
    }

    private boolean hasEnvironmentDirectory(Map<String, byte[]> entries) {
        return entries.keySet().stream()
                .anyMatch(path -> path.startsWith(BUNDLE_ROOT + "environments/default/"));
    }

    private Map<String, Map<String, byte[]>> groupDirectories(Map<String, byte[]> entries, String rootPrefix) {
        Map<String, Map<String, byte[]>> grouped = new LinkedHashMap<>();
        entries.forEach((path, bytes) -> {
            if (!path.startsWith(rootPrefix)) {
                return;
            }
            String remaining = path.substring(rootPrefix.length());
            int slash = remaining.indexOf('/');
            if (slash <= 0) {
                return;
            }
            String dir = rootPrefix + remaining.substring(0, slash);
            grouped.computeIfAbsent(dir, key -> new LinkedHashMap<>()).put(path, bytes);
        });
        return grouped;
    }

    private String lastSegment(String path) {
        String normalized = normalize(path);
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }

    private String normalize(String path) {
        return path.replace('\\', '/');
    }

    private boolean isPlatformJunk(String entryName) {
        if (!StringUtils.hasText(entryName)) {
            return true;
        }
        return entryName.startsWith("__MACOSX/")
                || entryName.equals(".DS_Store")
                || entryName.endsWith("/.DS_Store")
                || entryName.startsWith("._")
                || entryName.contains("/._");
    }

    private String asUtf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static final class ImportCollector {
        private String bundleId;
        private String bundleType;
        private String failedStage;
        private final List<String> completedStages = new ArrayList<>();
        private final Map<String, Integer> applied = new LinkedHashMap<>();
        private final List<BundleImportIssueResponse> issues = new ArrayList<>();
        private final List<BundleImportResourceResultResponse> resources = new ArrayList<>();

        private void applied(String key) {
            applied(key, 1);
        }

        private void applied(String key, int count) {
            applied.merge(key, count, Integer::sum);
        }

        private void resource(String stage, String type, String id, String status, String message) {
            resources.add(new BundleImportResourceResultResponse(stage, type, id, status, message));
        }

        private void completeStage(String stage) {
            completedStages.add(stage);
        }

        private void fail(String stage, String type, String id, String message, boolean fatal) {
            failedStage = stage;
            issues.add(new BundleImportIssueResponse(stage, type, id, message, fatal));
            resources.add(new BundleImportResourceResultResponse(stage, type, id, "FAILED", message));
        }

        private BundleImportResponse toResponse() {
            String status = issues.isEmpty()
                    ? "SUCCESS"
                    : completedStages.isEmpty() ? "FAILED" : "PARTIAL";
            return new BundleImportResponse(
                    bundleId != null ? bundleId : "unknown",
                    bundleType != null ? bundleType : "unknown",
                    status,
                    List.copyOf(completedStages),
                    failedStage,
                    Map.copyOf(applied),
                    List.copyOf(resources),
                    List.copyOf(issues));
        }
    }

    private record ImportedSkill(String skillId, String skillDir) {}

    private record BundleManifest(
            String schemaVersion,
            String bundleType,
            Boolean importEnvironment,
            String bundleId,
            String name,
            String description) {
        private boolean shouldImportEnvironment() {
            return Boolean.TRUE.equals(importEnvironment);
        }
    }

    private record RemoteSourceFile(
            String sourceId,
            String name,
            boolean enabled,
            long timeoutSeconds,
            long refreshIntervalSeconds,
            String mode,
            String metaUrl) {
    }

    private record McpServerFile(
            String serverId,
            String name,
            String transportType,
            boolean enabled,
            Map<String, Object> config,
            Map<String, String> headers,
            Map<String, String> queryParams,
            long timeoutSeconds,
            long initializationTimeoutSeconds) {
    }

    private record LocalToolsFile(List<LocalToolBindingFile> tools) {}

    private record LocalToolBindingFile(String toolName, boolean enabled) {}

    private record RemoteSourcesFile(List<RemoteSourceBindingFile> sources) {}

    private record RemoteSourceBindingFile(String sourceId, boolean enabled) {}

    private record McpServersFile(List<McpServerBindingFile> servers) {}

    private record McpServerBindingFile(
            String serverId,
            boolean enabled,
            List<String> enableTools,
            List<String> disableTools) {
    }
}
