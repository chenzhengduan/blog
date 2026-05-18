package xiaogj.xzagent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import xiaogj.xzagent.config.XzagentHtmlScreenshotProperties;
import xiaogj.xzagent.service.ThirdPartyCredentialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 将 HTML 字符串渲染为 PNG 截图的本地工具。
 *
 * <p>支持：
 * <ul>
 *     <li>直接传入 HTML 字符串</li>
 *     <li>可选宽高与等待时间</li>
 *     <li>截图 PNG 上传到服务端配置的地址，使用第三方凭证鉴权</li>
 * </ul>
 *
 * <p>上传地址和凭证 ID 由服务端配置注入，对 AI 完全不可见。
 * 当前不支持导航外部 URL、多页面流程或复杂浏览器交互，以保持安全边界和运行时复杂度可控。
 */
public class HtmlScreenshotTool implements AgentTool {

    private static final Logger log = LoggerFactory.getLogger(HtmlScreenshotTool.class);

    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;
    private static final int DEFAULT_WAIT_MS = 200;
    private static final int MAX_WAIT_MS = 5000;
    private static final int MAX_HTML_LENGTH = 200_000;
    private static final int MAX_WIDTH = 4096;
    private static final int MAX_HEIGHT = 4096;
    private static final int MAX_SCREENSHOT_FILES = 200;
    private static final Duration SCREENSHOT_RETENTION = Duration.ofHours(24);
    private static final Duration UPLOAD_TIMEOUT = Duration.ofSeconds(30);
    private static final Path OUTPUT_DIR = Path.of("/tmp/xzagent-html-screenshots");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(UPLOAD_TIMEOUT)
            .build();

    /** HTML 截图工具配置，包含上传地址和凭证 ID，由服务端注入，AI 不可见。 */
    private final XzagentHtmlScreenshotProperties properties;

    /** Jackson ObjectMapper，用于解析上传接口响应。 */
    private final ObjectMapper objectMapper;

    /** 第三方凭证服务，用于解析凭证 ID 对应的 HTTP 请求头。 */
    private final ThirdPartyCredentialService credentialService;

    public HtmlScreenshotTool(
            XzagentHtmlScreenshotProperties properties,
            ObjectMapper objectMapper,
            ThirdPartyCredentialService credentialService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.credentialService = credentialService;
    }

    @Override
    public String getName() {
        return "html_to_png_screenshot";
    }

    @Override
    public String getDescription() {
        return "将 HTML 字符串渲染成 PNG 截图后上传，并返回上传后的图片 URL";
    }

    @Override
    public Map<String, Object> getParameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "html", Map.of(
                                "type", "string",
                                "description", "要渲染的 HTML 字符串"),
                        "width", Map.of(
                                "type", "integer",
                                "description", "截图宽度，默认 1280"),
                        "height", Map.of(
                                "type", "integer",
                                "description", "截图高度，默认 720"),
                        "waitMs", Map.of(
                                "type", "integer",
                                "description", "渲染后等待毫秒数，默认 200，最大 5000"),
                        "fullPage", Map.of(
                                "type", "boolean",
                                "description", "是否截整页，默认 false")),
                "required", List.of("html"));
    }

    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        // 从服务端注入的用户上下文中取出 userId，AI 不可见
        UserToolContext userCtx = param.getContext() != null
                ? param.getContext().get(UserToolContext.class) : null;
        Long userId = userCtx != null ? userCtx.getUserId() : null;
        return Mono.fromCallable(() -> renderAndUpload(param.getInput(), userId))
                .subscribeOn(Schedulers.boundedElastic())
                .map(url -> ToolResultBlock.of(
                        TextBlock.builder()
                                .text(url)
                                .build()));
    }

    /**
     * 统一输出目录，供截图工具和文件访问控制器复用。
     */
    public static Path outputDirectory() {
        return OUTPUT_DIR;
    }

    /**
     * 执行一次 HTML 到 PNG 的渲染，并上传到配置的地址。
     *
     * <p>Playwright 与浏览器启动属于阻塞 IO，因此调用方应在 boundedElastic
     * 线程池中运行该方法。
     */
    private String renderAndUpload(Map<String, Object> input, Long userId)
            throws IOException, InterruptedException {
        String html = requiredHtml(input.get("html"));
        // 上传地址来自服务端配置，对 AI 不可见
        URI uploadUri = resolveConfiguredUploadUri();
        // 如配置了凭证 ID，解析用户的第三方凭证请求头用于鉴权上传
        Map<String, String> credentialHeaders = resolveCredentialHeaders(userId);

        int width = normalizeBoundedPositiveInt(input.get("width"), DEFAULT_WIDTH, MAX_WIDTH, "width");
        int height = normalizeBoundedPositiveInt(input.get("height"), DEFAULT_HEIGHT, MAX_HEIGHT, "height");
        int waitMs = Math.min(
                normalizeNonNegativeInt(input.get("waitMs"), DEFAULT_WAIT_MS),
                MAX_WAIT_MS);
        boolean fullPage = Boolean.TRUE.equals(input.get("fullPage"));

        Path outputDir = Files.createDirectories(outputDirectory());
        cleanupOldScreenshots(outputDir);
        Path outputFile = outputDir.resolve(
                "html-shot-" + UUID.randomUUID() + ".png");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true));
            try (Browser ignored = browser) {
                Page page = browser.newPage(new Browser.NewPageOptions()
                        .setViewportSize(width, height));
                blockExternalRequests(page);
                page.setContent(html);
                if (waitMs > 0) {
                    page.waitForTimeout(waitMs);
                }
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(outputFile)
                        .setFullPage(fullPage));
            }
        }
        return uploadScreenshot(outputFile, uploadUri, credentialHeaders);
    }

    /**
     * 校验 HTML 文本，避免空值或超大文本把浏览器渲染拖垮。
     */
    private String requiredHtml(Object value) {
        String html = value != null ? String.valueOf(value) : "";
        if (html.isBlank()) {
            throw new IllegalArgumentException("html 不能为空");
        }
        if (html.length() > MAX_HTML_LENGTH) {
            throw new IllegalArgumentException("html 长度不能超过 " + MAX_HTML_LENGTH);
        }
        return html;
    }

    /**
     * 从服务端配置解析并校验上传 URI，确保未配置时给出清晰的错误提示。
     */
    private URI resolveConfiguredUploadUri() {
        String uploadUrl = properties.uploadUrl() != null ? properties.uploadUrl().trim() : "";
        if (uploadUrl.isEmpty()) {
            throw new IllegalStateException(
                    "HTML 截图工具上传地址未配置，请设置 xzagent.tools.html-screenshot.upload-url");
        }
        try {
            URI uri = URI.create(uploadUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalStateException("xzagent.tools.html-screenshot.upload-url 必须是完整的 http/https 地址");
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "xzagent.tools.html-screenshot.upload-url 格式不正确: " + uploadUrl, e);
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
            log.warn("html_screenshot: 凭证 {} 不存在或未启用，将不附加凭证请求头", credentialId);
        }
        return headers;
    }

    /**
     * 阻止页面在截图过程中访问外部资源。
     *
     * <p>工具目标是"渲染给定 HTML"，而不是浏览互联网。因此第一版默认禁止
     * `http/https/file` 等外部资源加载，只允许 `about:`、`data:`、`blob:`
     * 这类内联资源，避免把安全边界描述和真实行为搞成两张皮。
     */
    private void blockExternalRequests(Page page) {
        page.route("**/*", route -> {
            String url = route.request().url();
            if (url.startsWith("about:")
                    || url.startsWith("data:")
                    || url.startsWith("blob:")) {
                route.resume();
            } else {
                route.abort();
            }
        });
    }

    /**
     * 将生成的 PNG 原始文件流上传到配置的接口地址，并附加第三方凭证请求头。
     *
     * @param outputFile        本地截图文件路径
     * @param uploadUri         服务端配置的上传目标地址
     * @param credentialHeaders 第三方凭证解析出的 HTTP 请求头，可为空 Map
     */
    private String uploadScreenshot(
            Path outputFile,
            URI uploadUri,
            Map<String, String> credentialHeaders)
            throws IOException, InterruptedException {
        byte[] fileBytes = Files.readAllBytes(outputFile);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uploadUri)
                .timeout(UPLOAD_TIMEOUT)
                .header("Content-Type", "image/png")
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes));
        // 附加第三方凭证请求头（如 Authorization），凭证来自服务端，AI 不可见
        credentialHeaders.forEach(builder::header);
        HttpRequest request = builder.build();
        HttpResponse<String> response = HTTP_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return parseUploadResponse(response);
    }

    /**
     * 解析上传接口返回值。
     *
     * <p>为了兼容不同实现，这里优先按 JSON 对象读取 `url` / `errorMsg`。
     * 如果响应本身就是一个纯文本 URL，也接受并直接返回。
     */
    private String parseUploadResponse(HttpResponse<String> response) throws IOException {
        String responseBody = response.body() != null ? response.body().trim() : "";
        log.info(
                "HTML 截图上传接口原始响应: statusCode={}, rawResponse={}",
                response.statusCode(),
                responseBody);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(extractUploadErrorMessage(
                    responseBody,
                    "图片上传失败，状态码: " + response.statusCode()));
        }

        if (responseBody.isEmpty()) {
            throw new IllegalStateException("图片上传成功但返回体为空，缺少 url");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
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
        } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {
            // 返回体不是 JSON 时，继续按纯文本 URL 处理。
        }

        if (looksLikeUrl(responseBody)) {
            return responseBody;
        }
        throw new IllegalStateException("图片上传成功但未返回 url");
    }

    /**
     * 从上传接口返回值中提取可读错误信息。
     */
    private String extractUploadErrorMessage(String responseBody, String defaultMessage) {
        if (responseBody == null || responseBody.isBlank()) {
            return defaultMessage;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isObject()) {
                JsonNode errorNode = root.get("errorMsg");
                if (errorNode != null && !errorNode.asText().isBlank()) {
                    return errorNode.asText().trim();
                }
            }
            if (root.isTextual() && !root.asText().isBlank()) {
                return root.asText().trim();
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {
            // 不是 JSON 时继续走纯文本兜底。
        }
        return responseBody;
    }

    private boolean looksLikeUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private int normalizePositiveInt(Object rawValue, int defaultValue) {
        int value = normalizeNonNegativeInt(rawValue, defaultValue);
        return value > 0 ? value : defaultValue;
    }

    /**
     * 归一化带上限的正整数参数。
     *
     * <p>截图宽高会直接影响浏览器内存占用，因此这里必须有硬上限，避免 Agent
     * 传入异常尺寸把 Chromium / JVM 压垮。
     */
    private int normalizeBoundedPositiveInt(
            Object rawValue,
            int defaultValue,
            int maxValue,
            String fieldName) {
        int value = normalizePositiveInt(rawValue, defaultValue);
        if (value > maxValue) {
            throw new IllegalArgumentException(
                    fieldName + " 不能超过 " + maxValue);
        }
        return value;
    }

    private int normalizeNonNegativeInt(Object rawValue, int defaultValue) {
        if (rawValue == null) {
            return defaultValue;
        }
        if (rawValue instanceof Number number) {
            return Math.max(number.intValue(), 0);
        }
        try {
            return Math.max(Integer.parseInt(String.valueOf(rawValue)), 0);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("数值参数格式不正确: " + rawValue, error);
        }
    }

    /**
     * 轻量清理历史截图文件。
     *
     * <p>第一版不做复杂的生命周期任务调度，但至少在每次生成截图前做一次轻量
     * 回收，避免 `/tmp/xzagent-html-screenshots` 无限膨胀。
     */
    private void cleanupOldScreenshots(Path outputDir) {
        try (Stream<Path> fileStream = Files.list(outputDir)) {
            List<Path> files = fileStream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(this::safeLastModified))
                    .toList();

            Instant expiredBefore = Instant.now().minus(SCREENSHOT_RETENTION);
            for (Path file : files) {
                if (safeLastModified(file).isBefore(expiredBefore)) {
                    Files.deleteIfExists(file);
                }
            }

            if (files.size() > MAX_SCREENSHOT_FILES) {
                int overflowCount = files.size() - MAX_SCREENSHOT_FILES;
                for (int index = 0; index < overflowCount; index++) {
                    Files.deleteIfExists(files.get(index));
                }
            }
        } catch (IOException ignored) {
            // 清理失败不阻断主流程，避免截图功能因为辅助清理问题直接不可用。
        }
    }

    private Instant safeLastModified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException error) {
            return Instant.EPOCH;
        }
    }
}
