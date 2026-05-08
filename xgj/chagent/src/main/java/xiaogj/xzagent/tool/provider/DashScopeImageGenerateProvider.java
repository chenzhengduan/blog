package xiaogj.xzagent.tool.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于阿里云 DashScope 万相系列的图像生成供应商实现。
 *
 * <p>调用 DashScope 多模态生成同步接口，将通用入参转换为 DashScope 协议后发起请求，
 * 返回临时图片 URL 列表。下载与持久化上传由上层工具统一处理。
 *
 * <p>参数说明：
 * <ul>
 *   <li>size：通用格式 {@code 1024x1024} 自动转换为 DashScope 格式 {@code 1024*1024}</li>
 *   <li>negative_prompt：透传至 DashScope parameters</li>
 *   <li>seed：透传至 DashScope parameters</li>
 * </ul>
 */
public class DashScopeImageGenerateProvider implements ImageGenerateProvider {

    private static final Logger log = LoggerFactory.getLogger(DashScopeImageGenerateProvider.class);

    /** DashScope 多模态生成同步接口地址。 */
    private static final String DASHSCOPE_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    /** DashScope API 密钥。 */
    private final String apiKey;

    /** 平台配置的默认生图模型，如 wan2.7-image。 */
    private final String model;

    /** Jackson ObjectMapper，用于序列化请求体和反序列化响应。 */
    private final ObjectMapper objectMapper;

    /** HTTP 客户端，连接超时 30s，生图请求超时 5 分钟。 */
    private final HttpClient httpClient;

    /**
     * @param apiKey       DashScope API 密钥
     * @param model        默认生图模型名称
     * @param objectMapper Jackson ObjectMapper
     */
    public DashScopeImageGenerateProvider(String apiKey, String model, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String getProviderName() {
        return "dashscope";
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public List<String> generateTemporaryUrls(ImageGenerateParams params) throws Exception {
        String requestJson = buildRequestJson(params);
        log.info("DashScope 生图请求体: {}", requestJson);
        HttpResponse<String> response = sendRequest(requestJson);
        log.info("DashScope 生图响应体: {}", response.body());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        validateResponse(response, responseMap);
        return extractUrls(responseMap);
    }

    /**
     * 构建 DashScope 多模态生图请求体。
     *
     * <p>size 格式从通用 {@code 1024x1024} 转为 DashScope 的 {@code 1024*1024}，
     * 并等比缩放确保总像素在 DashScope 允许范围内。同时在 prompt 中追加宽高比提示。
     * enable_sequential、thinking_mode、watermark 不开放给工具层，固定不传（使用服务默认值）。
     */
    private String buildRequestJson(ImageGenerateParams params) throws JsonProcessingException {
        // 解析尺寸，等比缩放到 DashScope 合法像素范围
        int[] original = ImageSizeUtils.parseSize(params.size());
        int[] adjusted = ImageSizeUtils.adjustForDashScope(original[0], original[1]);
        if (adjusted[0] != original[0] || adjusted[1] != original[1]) {
            log.info("DashScope 尺寸等比调整: {}x{} -> {}x{}", original[0], original[1], adjusted[0], adjusted[1]);
        }
        String dashScopeSize = adjusted[0] + "*" + adjusted[1];

        // 在 prompt 中追加原始宽高比提示，引导模型按比例生成
        String prompt = ImageSizeUtils.appendRatioHint(params.prompt(), original[0], original[1]);

        // 组装 content：文本在前，参考图在后
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of("text", prompt));
        for (String image : params.images()) {
            content.add(Map.of("image", image));
        }

        // 组装 parameters，按需传入可选参数
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("size", dashScopeSize);
        parameters.put("n", params.n());
        if (params.negativePrompt() != null && !params.negativePrompt().isBlank()) {
            parameters.put("negative_prompt", params.negativePrompt());
        }
        if (params.seed() != null) {
            parameters.put("seed", params.seed());
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", Map.of(
                "messages", List.of(Map.of("role", "user", "content", content))));
        requestBody.put("parameters", parameters);
        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * 发送 HTTP 请求，生图超时设为 5 分钟。
     */
    private HttpResponse<String> sendRequest(String requestJson) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DASHSCOPE_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMinutes(5))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 校验 DashScope 返回的 HTTP 状态与业务错误码。
     */
    private void validateResponse(HttpResponse<String> response, Map<String, Object> responseMap) {
        if (responseMap.containsKey("code")) {
            String code = String.valueOf(responseMap.get("code"));
            String message = String.valueOf(responseMap.getOrDefault("message", "未知错误"));
            log.error("DashScope 生图 API 错误: code={}, message={}", code, message);
            throw new ImageGenerateException("生图失败，错误码: " + code + "，原因: " + message);
        }
        if (response.statusCode() != 200) {
            log.error("DashScope 生图 HTTP 错误: status={}", response.statusCode());
            throw new ImageGenerateException("生图失败，HTTP 状态码: " + response.statusCode());
        }
    }

    /**
     * 从 DashScope 响应的 output.choices 中提取图片临时 URL。
     */
    @SuppressWarnings("unchecked")
    private List<String> extractUrls(Map<String, Object> responseMap) {
        Map<String, Object> output = (Map<String, Object>) responseMap.get("output");
        if (output == null) {
            throw new ImageGenerateException("响应格式异常：缺少 output 字段");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) output.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new ImageGenerateException("未生成任何图片");
        }

        List<String> urls = new ArrayList<>();
        for (Map<String, Object> choice : choices) {
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            if (message == null) {
                continue;
            }
            List<Map<String, Object>> contentItems = (List<Map<String, Object>>) message.get("content");
            if (contentItems == null) {
                continue;
            }
            for (Map<String, Object> item : contentItems) {
                if ("image".equals(item.get("type"))) {
                    String url = (String) item.get("image");
                    if (url != null && !url.isBlank()) {
                        urls.add(url);
                        log.info("DashScope 生图临时 URL: {}", url);
                    }
                }
            }
        }

        if (urls.isEmpty()) {
            throw new ImageGenerateException("响应中未找到图片 URL");
        }
        return urls;
    }
}
