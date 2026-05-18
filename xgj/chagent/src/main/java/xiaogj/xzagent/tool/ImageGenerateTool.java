package xiaogj.xzagent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import xiaogj.xzagent.config.XzagentImageGenerateProperties;
import xiaogj.xzagent.model.ImageGenerateUsage;
import xiaogj.xzagent.repository.ImageGenerateUsageRepository;
import xiaogj.xzagent.service.ThirdPartyCredentialService;
import xiaogj.xzagent.tool.provider.ImageGenerateParams;
import xiaogj.xzagent.tool.provider.ImageGenerateProvider;

/**
 * 通用图像生成与编辑工具。
 *
 * <p>支持以下场景：
 * <ul>
 *   <li>文生图：仅提供文本提示词，根据描述生成图片</li>
 *   <li>图像编辑：提供参考图片 + 文本指令，按指令修改图片</li>
 * </ul>
 *
 * <p>上传地址和凭证 ID 由服务端配置注入，对 AI 完全不可见，避免暴露内部接口。
 * 工具生成图片后下载临时 URL，使用用户的第三方凭证请求头上传到持久化地址，并返回 URL 列表。
 * 底层供应商通过 {@link ImageGenerateProvider} 接口注入，支持 DashScope、OpenAI 等多种实现。
 */
public class ImageGenerateTool implements AgentTool {

    private static final Logger log = LoggerFactory.getLogger(ImageGenerateTool.class);

    /** 图片下载与上传超时。 */
    private static final Duration UPLOAD_TIMEOUT = Duration.ofSeconds(30);

    /** 上传时的兜底内容类型。 */
    private static final String DEFAULT_UPLOAD_CONTENT_TYPE = "image/png";

    /** 图像生成供应商，负责调用各自 API 返回临时 URL。 */
    private final ImageGenerateProvider provider;

    /** Jackson ObjectMapper，用于序列化结果和解析上传响应。 */
    private final ObjectMapper objectMapper;

    /** HTTP 客户端，用于下载临时图片和上传到持久化地址。 */
    private final HttpClient httpClient;

    /** 图像生成工具配置，包含上传地址和凭证 ID，由服务端注入，AI 不可见。 */
    private final XzagentImageGenerateProperties properties;

    /** 第三方凭证服务，用于解析凭证 ID 对应的 HTTP 请求头。 */
    private final ThirdPartyCredentialService credentialService;

    /** 图片生成用量记录仓库，用于按张计费与对账。 */
    private final ImageGenerateUsageRepository imageGenerateUsageRepository;

    /**
     * @param provider          图像生成供应商实现
     * @param objectMapper      Jackson ObjectMapper
     * @param properties        图像生成工具配置（上传 URL、凭证 ID）
     * @param credentialService 第三方凭证服务
     */
    public ImageGenerateTool(
            ImageGenerateProvider provider,
            ObjectMapper objectMapper,
            XzagentImageGenerateProperties properties,
            ThirdPartyCredentialService credentialService,
            ImageGenerateUsageRepository imageGenerateUsageRepository) {
        this.provider = provider;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.credentialService = credentialService;
        this.imageGenerateUsageRepository = imageGenerateUsageRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String getName() {
        return "image_generate";
    }

    @Override
    public String getDescription() {
        return "图像生成与编辑工具。根据文本描述生成图片，或基于参考图片进行编辑，生成后返回可持久化的图片 URL 列表。";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "prompt", Map.of(
                                "type", "string",
                                "description", "图像生成的文本提示词，描述想要生成的内容，支持中英文，最多5000字符"),
                        "negative_prompt", Map.of(
                                "type", "string",
                                "description", "反向提示词，描述不希望出现在图片中的内容，不支持的供应商会忽略此参数"),
                        "images", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string"),
                                "description", "参考图片列表，图像编辑时使用。每项为图片公网URL或Base64编码（data:image/jpeg;base64,xxx），最多9张"),
                        "size", Map.of(
                                "type", "string",
                                "description", "输出分辨率，格式为 宽x高，如 1024x1024、1792x1024、1024x1792，默认 1024x1024"),
                        "n", Map.of(
                                "type", "integer",
                                "description", "生成图片数量，默认1"),
                        "seed", Map.of(
                                "type", "integer",
                                "description", "随机数种子（0-2147483647），相同seed可复现相近结果，不支持的供应商会忽略此参数")),
                "required", List.of("prompt"));
    }

    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        // 从服务端注入的用户上下文中取出 userId，AI 不可见
        UserToolContext userCtx = param.getContext() != null
                ? param.getContext().get(UserToolContext.class) : null;
        Long userId = userCtx != null ? userCtx.getUserId() : null;
        // 从服务端注入的运行上下文中取出 sessionId / runId，用于用量记录
        AgentRunToolContext runCtx = param.getContext() != null
                ? param.getContext().get(AgentRunToolContext.class) : null;
        String sessionId = runCtx != null ? runCtx.sessionId() : null;
        String runId = runCtx != null ? runCtx.runId() : null;
        return Mono.fromCallable(() -> generate(param.getInput(), userId, sessionId, runId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 解析入参、调用供应商生成图片、下载并上传到持久化地址。
     *
     * @param input 工具调用入参
     * @param userId 发起请求的用户 ID，用于查询第三方凭证请求头（可为 null）
     * @param sessionId 当前会话 ID，用于图片用量记录（可为 null）
     * @param runId 当前运行 ID，用于图片用量记录（可为 null）
     */
    private ToolResultBlock generate(Map<String, Object> input, Long userId, String sessionId, String runId) {
        try {
            // 从服务端配置获取上传地址，避免 AI 自行指定上传目标
            URI uploadUri = resolveConfiguredUploadUri();

            // 如配置了凭证 ID，则解析用户的第三方凭证请求头用于鉴权上传
            Map<String, String> credentialHeaders = resolveCredentialHeaders(userId);

            ImageGenerateParams params = buildParams(input, uploadUri);
            List<String> temporaryUrls = provider.generateTemporaryUrls(params);
            List<String> persistentUrls = uploadGeneratedImages(temporaryUrls, uploadUri, credentialHeaders);
            if (persistentUrls.isEmpty()) {
                return ToolResultBlock.error("未获得任何可持久化图片 URL");
            }
            recordImageGenerateUsage(userId, sessionId, runId, params, persistentUrls.size());
            log.info("image_generate 完成: userId={}, runId={}, provider={}, model={}, 生成 {} 张，成功上传 {} 张",
                    userId, runId, provider.getProviderName(), provider.getModelName(), temporaryUrls.size(), persistentUrls.size());
            return buildUrlResult(persistentUrls);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("image_generate 被中断", e);
            return ToolResultBlock.error("生图被中断");
        } catch (Exception e) {
            log.error("image_generate 失败: {}", e.getMessage(), e);
            return ToolResultBlock.error("生图失败: " + e.getMessage());
        }
    }

    /**
     * 记录图片生成用量。
     *
     * <p>仅在成功获得至少一张持久化图片后写入一条记录，按实际成功张数统计。
     * 写库失败由 Repository 内部兜底，不影响工具主链路。
     */
    private void recordImageGenerateUsage(
            Long userId,
            String sessionId,
            String runId,
            ImageGenerateParams params,
            int imageCount) {
        if (userId == null || sessionId == null || runId == null || imageCount <= 0) {
            log.warn("跳过图片生成用量记录: userId={}, sessionId={}, runId={}, imageCount={}",
                    userId, sessionId, runId, imageCount);
            return;
        }
        imageGenerateUsageRepository.save(new ImageGenerateUsage(
                userId,
                sessionId,
                runId,
                provider.getProviderName(),
                provider.getModelName(),
                params.size(),
                imageCount));
    }

    /**
     * 从工具调用输入构建标准化参数对象。
     *
     * @param input     工具调用入参（不含 uploadUrl，由服务端配置提供）
     * @param uploadUri 服务端配置的上传地址
     */
    @SuppressWarnings("unchecked")
    private ImageGenerateParams buildParams(Map<String, Object> input, URI uploadUri) {
        String prompt = getRequiredString(input, "prompt");
        String negativePrompt = getOptionalString(input, "negative_prompt");
        List<String> images = input.get("images") instanceof List<?> list
                ? (List<String>) list : Collections.emptyList();
        String size = getStringOrDefault(input, "size", ImageGenerateParams.DEFAULT_SIZE);
        int n = input.get("n") instanceof Number num ? num.intValue() : 1;
        Integer seed = input.get("seed") instanceof Number num ? num.intValue() : null;
        return new ImageGenerateParams(prompt, negativePrompt, uploadUri, size, n, images, seed);
    }

    /**
     * 将供应商返回的临时 URL 逐张下载并上传为持久化 URL。
     *
     * <p>单张失败只记录日志并跳过，不中断其他图片的处理。
     */
    private List<String> uploadGeneratedImages(
            List<String> temporaryUrls,
            URI uploadUri,
            Map<String, String> credentialHeaders) {
        List<String> persistentUrls = new ArrayList<>();
        for (int i = 0; i < temporaryUrls.size(); i++) {
            String temporaryUrl = temporaryUrls.get(i);
            int index = i + 1;
            try {
                String persistentUrl = uploadSingleImage(temporaryUrl, uploadUri, credentialHeaders);
                persistentUrls.add(persistentUrl);
                log.info("第 {} 张图片上传成功: sourceUrl={}, uploadedUrl={}", index, temporaryUrl, persistentUrl);
            } catch (Exception e) {
                log.error("第 {} 张图片上传失败: sourceUrl={}, message={}", index, temporaryUrl, e.getMessage(), e);
            }
        }
        return persistentUrls;
    }

    private String uploadSingleImage(
            String temporaryUrl,
            URI uploadUri,
            Map<String, String> credentialHeaders) throws Exception {
        DownloadedImage image = downloadImageBytes(temporaryUrl);
        return uploadImageBytes(image, uploadUri, credentialHeaders);
    }

    /**
     * 从临时 URL 或 data URI 获取图片原始字节。
     *
     * <p>若为 {@code data:} 开头的 data URI（如 base64 响应），直接解码，不发 HTTP 请求。
     * 否则按普通 URL 发起 GET 下载。
     */
    private DownloadedImage downloadImageBytes(String imageUrl) throws Exception {
        if (imageUrl.startsWith("data:")) {
            return decodeDataUri(imageUrl);
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(imageUrl))
                .timeout(UPLOAD_TIMEOUT)
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("下载图片失败，状态码: " + response.statusCode());
        }
        byte[] body = response.body();
        if (body == null || body.length == 0) {
            throw new IllegalStateException("下载图片失败，响应体为空");
        }
        String contentType = response.headers()
                .firstValue("Content-Type")
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .orElse(DEFAULT_UPLOAD_CONTENT_TYPE);
        return new DownloadedImage(body, contentType);
    }

    /**
     * 解码 data URI，提取图片字节和内容类型。
     *
     * <p>格式：{@code data:<mediatype>;base64,<data>}，如 {@code data:image/png;base64,iVBOR...}
     */
    private DownloadedImage decodeDataUri(String dataUri) {
        // 去掉 "data:" 前缀，分割 mediatype 与 base64 数据
        String rest = dataUri.substring("data:".length());
        int commaIndex = rest.indexOf(',');
        if (commaIndex < 0) {
            throw new IllegalStateException("data URI 格式不正确，缺少逗号分隔符");
        }
        String meta = rest.substring(0, commaIndex);
        String base64Data = rest.substring(commaIndex + 1);

        // meta 形如 "image/png;base64"，取分号前的内容类型
        String contentType = meta.contains(";") ? meta.substring(0, meta.indexOf(';')) : meta;
        if (contentType.isBlank()) {
            contentType = DEFAULT_UPLOAD_CONTENT_TYPE;
        }

        byte[] bytes = Base64.getDecoder().decode(base64Data);
        return new DownloadedImage(bytes, contentType);
    }

    /**
     * 将图片原始字节 POST 到配置的上传地址，并附加第三方凭证请求头。
     *
     * @param image             已下载的图片二进制和内容类型
     * @param uploadUri         服务端配置的上传目标地址
     * @param credentialHeaders 第三方凭证解析出的 HTTP 请求头，可为空 Map
     */
    private String uploadImageBytes(
            DownloadedImage image,
            URI uploadUri,
            Map<String, String> credentialHeaders) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uploadUri)
                .timeout(UPLOAD_TIMEOUT)
                .header("Content-Type", image.contentType())
                .POST(HttpRequest.BodyPublishers.ofByteArray(image.bytes()));
        // 附加第三方凭证请求头（如 Authorization），凭证来自服务端，AI 不可见
        credentialHeaders.forEach(builder::header);
        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseUploadResponse(response);
    }

    /**
     * 解析上传接口响应，优先读取 JSON 中的 url 字段，兼容纯文本 URL。
     */
    private String parseUploadResponse(HttpResponse<String> response) throws Exception {
        String body = response.body() != null ? response.body().trim() : "";
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(extractUploadErrorMessage(
                    body, "图片上传失败，状态码: " + response.statusCode()));
        }
        if (body.isEmpty()) {
            throw new IllegalStateException("图片上传成功但返回体为空，缺少 url");
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.isObject()) {
                JsonNode urlNode = root.get("url");
                if (urlNode != null && !urlNode.asText().isBlank()) {
                    return urlNode.asText().trim();
                }
                JsonNode errorNode = root.get("errorMsg");
                if (errorNode != null && !errorNode.asText().isBlank()) {
                    throw new IllegalStateException(errorNode.asText().trim());
                }
            }
            if (root.isTextual() && !root.asText().isBlank()) {
                return root.asText().trim();
            }
        } catch (JsonProcessingException ignored) {
            // 返回体不是 JSON 时，继续按纯文本 URL 处理
        }

        if (body.startsWith("http://") || body.startsWith("https://")) {
            return body;
        }
        throw new IllegalStateException("图片上传成功但未返回 url");
    }

    /**
     * 从上传接口返回值中提取可读错误信息。
     */
    private String extractUploadErrorMessage(String body, String defaultMessage) {
        if (body == null || body.isBlank()) {
            return defaultMessage;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.isObject()) {
                JsonNode errorNode = root.get("errorMsg");
                if (errorNode != null && !errorNode.asText().isBlank()) {
                    return errorNode.asText().trim();
                }
            }
            if (root.isTextual() && !root.asText().isBlank()) {
                return root.asText().trim();
            }
        } catch (JsonProcessingException ignored) {
        }
        return body;
    }

    /**
     * 构造最终返回给 Agent 的 JSON 结果，包含持久化 URL 列表。
     */
    private ToolResultBlock buildUrlResult(List<String> urls) throws JsonProcessingException {
        return ToolResultBlock.of(TextBlock.builder()
                .text(objectMapper.writeValueAsString(Map.of("urls", urls)))
                .build());
    }

    /**
     * 从服务端配置解析并校验上传 URI，确保未配置时给出清晰的错误提示。
     */
    private URI resolveConfiguredUploadUri() {
        String uploadUrl = properties.uploadUrl() != null ? properties.uploadUrl().trim() : "";
        if (uploadUrl.isEmpty()) {
            throw new IllegalStateException(
                    "图像生成工具上传地址未配置，请设置 xzagent.tools.image-generate.upload-url");
        }
        try {
            URI uri = URI.create(uploadUrl);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || scheme == null
                    || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new IllegalStateException("xzagent.tools.image-generate.upload-url 必须是完整的 http/https 地址");
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "xzagent.tools.image-generate.upload-url 格式不正确: " + uploadUrl, e);
        }
    }

    /**
     * 解析用户对应的第三方凭证请求头。
     *
     * <p>仅在配置了凭证 ID 且 userId 不为 null 时才查询，否则返回空 Map。
     */
    private Map<String, String> resolveCredentialHeaders(Long userId) {
        String credentialId = properties.uploadCredentialId();
        if (userId == null || credentialId == null || credentialId.isBlank()) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = credentialService.resolveHeaders(userId, credentialId.trim());
        if (headers.isEmpty()) {
            log.warn("image_generate: 凭证 {} 不存在或未启用，将不附加凭证请求头", credentialId);
        }
        return headers;
    }

    private String getRequiredString(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value instanceof String str && !str.isBlank()) {
            return str;
        }
        throw new IllegalArgumentException(key + " 不能为空");
    }

    private String getOptionalString(Map<String, Object> input, String key) {
        Object value = input.get(key);
        return value instanceof String str && !str.isBlank() ? str : null;
    }

    private String getStringOrDefault(Map<String, Object> input, String key, String defaultValue) {
        Object value = input.get(key);
        return value instanceof String str && !str.isBlank() ? str : defaultValue;
    }

    /** 下载后的图片二进制与内容类型。 */
    private record DownloadedImage(byte[] bytes, String contentType) {}
}
