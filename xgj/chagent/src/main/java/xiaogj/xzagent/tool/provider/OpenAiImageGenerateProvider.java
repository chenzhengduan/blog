package xiaogj.xzagent.tool.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 OpenAI Images API 的图像生成供应商实现。
 *
 * <p>支持 dall-e-2、dall-e-3 等模型，也兼容 OpenAI API 格式的第三方服务（通过 baseUrl 配置）。
 *
 * <p>以下参数 OpenAI API 不支持，会被静默忽略：
 * <ul>
 *   <li>negative_prompt：OpenAI 无对应参数</li>
 *   <li>seed：OpenAI Images API 无随机种子支持</li>
 *   <li>images：图像编辑需要 multipart 接口，当前版本暂不支持</li>
 * </ul>
 *
 * <p>注意：dall-e-3 每次只能生成 1 张（n 固定为 1）。
 */
public class OpenAiImageGenerateProvider implements ImageGenerateProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiImageGenerateProvider.class);

    /** OpenAI Images 生成接口路径。 */
    private static final String IMAGES_GENERATIONS_PATH = "/v1/images/generations";

    /** 最大重试次数（不含首次请求）。 */
    private static final int MAX_RETRIES = 3;

    /** 首次重试等待时间（毫秒），后续指数递增：1s → 2s → 4s。 */
    private static final long RETRY_BASE_DELAY_MS = 1_000L;

    /** OpenAI API 密钥。 */
    private final String apiKey;

    /** 图像生成模型，如 dall-e-3。 */
    private final String model;

    /**
     * API 基础地址，支持代理或兼容服务。
     * 末尾不含斜杠，默认 https://api.openai.com。
     */
    private final String baseUrl;

    /** Jackson ObjectMapper。 */
    private final ObjectMapper objectMapper;

    /** HTTP 客户端，连接超时 30s，生图请求超时 5 分钟。 */
    private final HttpClient httpClient;

    /**
     * @param apiKey       OpenAI API 密钥
     * @param model        图像生成模型名称，如 dall-e-3
     * @param baseUrl      API 基础地址，null 或空时使用 https://api.openai.com
     * @param objectMapper Jackson ObjectMapper
     */
    public OpenAiImageGenerateProvider(String apiKey, String model, String baseUrl, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = (baseUrl != null && !baseUrl.isBlank())
                ? baseUrl.replaceAll("/+$", "")
                : "https://api.openai.com";
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public List<String> generateTemporaryUrls(ImageGenerateParams params) throws Exception {
        String requestJson = buildRequestJson(params);
        log.info("OpenAI 生图请求体: {}", requestJson);
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            HttpResponse<String> response = sendRequest(requestJson);
            log.info("OpenAI 生图响应体: {}", response.body());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return extractUrls(response.body());
            }
            String errorMessage = extractErrorMessage(response.body());
            if (attempt < MAX_RETRIES && isRetryable(response.statusCode(), errorMessage)) {
                long delay = RETRY_BASE_DELAY_MS << attempt;
                log.warn("OpenAI 生图错误可重试，第 {}/{} 次，等待 {}ms: status={}",
                        attempt + 1, MAX_RETRIES, delay, response.statusCode());
                Thread.sleep(delay);
                continue;
            }
            log.error("OpenAI 生图 HTTP 错误: status={}, message={}", response.statusCode(), errorMessage);
            throw new ImageGenerateException("生图失败，HTTP 状态码: " + response.statusCode() + "，原因: " + errorMessage);
        }
        throw new ImageGenerateException("生图失败：超过最大重试次数");
    }

    /**
     * 判断该错误是否值得重试。
     *
     * <p>429/500/502/503 属于服务端瞬时故障，直接重试；
     * 400 且错误信息为空，视为三方服务器超载导致的空响应，可重试；
     * 400 且有具体错误信息，视为参数错误，不重试。
     */
    private boolean isRetryable(int statusCode, String errorMessage) {
        return switch (statusCode) {
            case 429, 500, 502, 503 -> true;
            case 400 -> errorMessage == null || errorMessage.isBlank();
            default -> false;
        };
    }

    /**
     * 构建 OpenAI Images 生成请求体。
     *
     * <p>size 固定传 {@code auto}，由模型自行决定输出尺寸；
     * 同时将用户传入的宽高比追加到 prompt，引导模型按预期比例生成。
     * negative_prompt、seed、images 不被 OpenAI 支持，静默忽略。
     */
    private String buildRequestJson(ImageGenerateParams params) throws Exception {
        // 解析宽高比并追加到 prompt
        int[] size = ImageSizeUtils.parseSize(params.size());
        String prompt = ImageSizeUtils.appendRatioHint(params.prompt(), size[0], size[1]);

        return objectMapper.writeValueAsString(Map.of(
                "model", model,
                "prompt", prompt,
                "n", params.n(),
                "size", "auto"));
    }

    /**
     * 发送 HTTP 请求，生图超时设为 5 分钟。
     */
    private HttpResponse<String> sendRequest(String requestJson) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + IMAGES_GENERATIONS_PATH))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMinutes(5))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 从 OpenAI 错误响应中提取可读错误信息。
     */
    private String extractErrorMessage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode message = root.path("error").path("message");
            if (!message.isMissingNode() && !message.asText().isBlank()) {
                return message.asText();
            }
        } catch (Exception ignored) {
        }
        return body;
    }

    /**
     * 从 OpenAI 响应的 data 数组中提取图片 URL 或 Base64 数据。
     *
     * <p>url 字段直接返回；b64_json 字段包装为 data URI 返回，
     * 由上层工具统一解码并上传到持久化地址。
     */
    private List<String> extractUrls(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode data = root.path("data");
        if (!data.isArray() || data.isEmpty()) {
            throw new ImageGenerateException("OpenAI 响应中未找到图片数据");
        }

        List<String> urls = new ArrayList<>();
        for (JsonNode item : data) {
            String url = item.path("url").asText(null);
            if (url != null && !url.isBlank()) {
                urls.add(url);
                log.info("OpenAI 生图临时 URL: {}", url);
                continue;
            }
            // gpt-image-1 等模型默认返回 base64，包装为 data URI 交由上层处理
            String b64 = item.path("b64_json").asText(null);
            if (b64 != null && !b64.isBlank()) {
                urls.add("data:image/png;base64," + b64);
                log.info("OpenAI 生图返回 base64，已包装为 data URI");
            }
        }

        if (urls.isEmpty()) {
            throw new ImageGenerateException("OpenAI 响应中未找到图片 URL 或 base64 数据");
        }
        return urls;
    }
}
