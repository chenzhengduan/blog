package xiaogj.xzagent.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import xiaogj.xzagent.config.XzagentRemoteToolLoggingProperties;
import xiaogj.xzagent.model.remote.RemoteToolAuth;
import xiaogj.xzagent.model.remote.RemoteToolDefinition;
import xiaogj.xzagent.model.remote.RemoteToolMetaDocument;
import xiaogj.xzagent.model.remote.RemoteToolRequest;
import xiaogj.xzagent.model.remote.RemoteToolResponse;
import xiaogj.xzagent.model.remote.RemoteToolResponseMapping;
import xiaogj.xzagent.service.UserRemoteSourceCredentialBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * 基于远程元数据标准生成的 HTTP AgentTool。
 *
 * <p>该工具承担两层职责：
 * <ul>
 *     <li>把元数据标准中的工具定义暴露给 Agent，形成稳定的工具名、
 *     描述和输入参数结构。</li>
 *     <li>在运行时把 Agent 输入映射成真实 HTTP 请求，再将响应裁剪成
 *     适合 Agent 消费的文本结果。</li>
 * </ul>
 *
 * <p>当前实现刻意只支持轻量子集：
 * <ul>
 *     <li>GET / POST</li>
 *     <li>简单模板变量 `{{field}}` 替换</li>
 *     <li>`raw` / `json_path` 两类响应映射</li>
 * </ul>
 *
 * <p>这样做是为了让第一版保持可控，避免把运行时元数据格式演变成通用
 * 脚本语言。
 */
public class RemoteHttpAgentTool implements AgentTool {

    private static final Logger log =
            LoggerFactory.getLogger(RemoteHttpAgentTool.class);
    private static final Pattern TEMPLATE_TOKEN_PATTERN =
            Pattern.compile("\\{\\{\\s*([^{}]+?)\\s*}}");

    private final String sourceId;
    private final Long userId;
    private final String username;
    private final RemoteToolMetaDocument document;
    private final RemoteToolDefinition definition;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final UserRemoteSourceCredentialBindingService bindingService;
    private final XzagentRemoteToolLoggingProperties remoteToolLoggingProperties;

    public RemoteHttpAgentTool(
            String sourceId,
            Long userId,
            String username,
            RemoteToolMetaDocument document,
            RemoteToolDefinition definition,
            ObjectMapper objectMapper,
            WebClient webClient,
            UserRemoteSourceCredentialBindingService bindingService,
            XzagentRemoteToolLoggingProperties remoteToolLoggingProperties) {
        this.sourceId = sourceId;
        this.userId = userId;
        this.username = username;
        this.document = document;
        this.definition = definition;
        this.objectMapper = objectMapper;
        this.webClient = webClient;
        this.bindingService = bindingService;
        this.remoteToolLoggingProperties = remoteToolLoggingProperties;
    }

    @Override
    public String getName() {
        return definition.name();
    }

    @Override
    public String getDescription() {
        return definition.description();
    }

    @Override
    public Map<String, Object> getParameters() {
        if (definition.inputSchema() == null || definition.inputSchema().isEmpty()) {
            return Map.of(
                    "type", "object",
                    "properties", Map.of(),
                    "required", List.of());
        }
        return definition.inputSchema();
    }

    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        Map<String, Object> input = param.getInput() != null
                ? param.getInput()
                : Collections.emptyMap();

        // 在真正发起远程请求前先做一层工具级必填参数校验。
        // 这样可以避免把空值请求发到远程 API，导致排障时只能看到下游报错。
        validateRequiredInputs(input);

        // 当前实现只支持最常见的 GET / POST。更复杂方法在标准稳定后再逐步补充。
        String method = safeUpperCase(definition.method());
        log.info(
                "开始执行远程工具: userId={}, username={}, sourceId={}, toolName={}, method={}, inputKeys={}",
                userId,
                username,
                sourceId,
                definition.name(),
                method,
                input.keySet());
        Mono<ToolResultBlock> result = switch (method) {
            case "GET" -> executeGet(input);
            case "POST" -> executePost(input);
            default -> Mono.error(new IllegalStateException("暂不支持的远程工具 HTTP 方法: " + method));
        };
        return result.doOnSuccess(toolResult -> log.info(
                        "远程工具执行完成: userId={}, username={}, sourceId={}, toolName={}, resultTextLength={}",
                        userId,
                        username,
                        sourceId,
                        definition.name(),
                        extractResultLength(toolResult)))
                .doOnError(error -> log.error(
                        "远程工具执行失败: userId={}, username={}, sourceId={}, toolName={}, errorType={}, message={}",
                        userId,
                        username,
                        sourceId,
                        definition.name(),
                        error.getClass().getSimpleName(),
                        error.getMessage(),
                        error));
    }

    /**
     * 校验顶层必填输入字段。
     *
     * <p>当前远程工具模板替换对顶层字段最稳定，因此这里也只对
     * `inputSchema.required` 中声明的顶层字段做强校验。若后续需要支持深层
     * required 校验，应与模板解析能力一起演进，而不是在这里单独做半套递归校验。
     */
    @SuppressWarnings("unchecked")
    private void validateRequiredInputs(Map<String, Object> input) {
        if (definition.inputSchema() == null || definition.inputSchema().isEmpty()) {
            return;
        }

        Object requiredValue = definition.inputSchema().get("required");
        if (!(requiredValue instanceof List<?> requiredFields) || requiredFields.isEmpty()) {
            return;
        }

        List<String> missingFields = new ArrayList<>();
        for (Object field : requiredFields) {
            if (!(field instanceof String fieldName) || fieldName.isBlank()) {
                continue;
            }
            Object value = input.get(fieldName);
            if (!input.containsKey(fieldName) || isMissingRequiredValue(value)) {
                missingFields.add(fieldName);
            }
        }

        if (!missingFields.isEmpty()) {
            log.warn(
                    "远程工具缺少必填入参: userId={}, username={}, sourceId={}, toolName={}, missingFields={}",
                    userId,
                    username,
                    sourceId,
                    definition.name(),
                    missingFields);
            throw new IllegalArgumentException(
                    "远程工具缺少必填入参: " + String.join(", ", missingFields));
        }
    }

    /**
     * 判断一个值是否应被视为“缺少必填值”。
     *
     * <p>规则保持保守：
     * <ul>
     *     <li>{@code null} 视为缺失</li>
     *     <li>空白字符串视为缺失</li>
     *     <li>空集合或空 Map 视为缺失</li>
     * </ul>
     */
    private boolean isMissingRequiredValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String stringValue) {
            return stringValue.isBlank();
        }
        if (value instanceof List<?> listValue) {
            return listValue.isEmpty();
        }
        if (value instanceof Map<?, ?> mapValue) {
            return mapValue.isEmpty();
        }
        return false;
    }

    /**
     * 执行 GET 请求。
     *
     * <p>GET 工具主要依赖 path/query 参数映射，不使用请求体。
     */
    private Mono<ToolResultBlock> executeGet(Map<String, Object> input) {
        String url = buildAbsoluteUrl(input);
        Map<String, String> queryParams = resolveQueryParams(input);
        logRequestOverview("GET", url, queryParams, Collections.emptyMap(), input);
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                .uri(buildUri(url, queryParams));
        return enrichHeaders(requestSpec, input)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(resolveTimeout())
                .doOnNext(responseBody -> logRawResponseIfEnabled("GET", url, responseBody))
                .map(this::mapResponseToBlock);
    }

    /**
     * 执行 POST 请求。
     *
     * <p>POST 工具除了 path/query 参数外，还会基于 `bodyTemplate` 组装请求体。
     */
    private Mono<ToolResultBlock> executePost(Map<String, Object> input) {
        String url = buildAbsoluteUrl(input);
        Map<String, Object> body = resolveBodyTemplate(input);
        Map<String, String> queryParams = resolveQueryParams(input);
        logRequestOverview("POST", url, queryParams, body, input);
        WebClient.RequestBodySpec requestSpec = webClient.post()
                .uri(buildUri(url, queryParams));
        applyHeaders(requestSpec, input);
        WebClient.RequestHeadersSpec<?> headersSpec = applyBody(requestSpec, body);
        return headersSpec.retrieve()
                .bodyToMono(String.class)
                .timeout(resolveTimeout())
                .doOnNext(responseBody -> logRawResponseIfEnabled("POST", url, responseBody))
                .map(this::mapResponseToBlock);
    }

    /**
     * 统一补充头部信息，包括：
     * <ul>
     *     <li>默认头</li>
     *     <li>工具级头</li>
     *     <li>请求映射里的额外头</li>
     *     <li>认证头</li>
     * </ul>
     */
    private WebClient.RequestHeadersSpec<?> enrichHeaders(
            WebClient.RequestHeadersSpec<?> requestSpec,
            Map<String, Object> input) {
        applyHeaders(requestSpec, input);
        return requestSpec;
    }

    /**
     * 统一向请求对象写入头部信息。
     *
     * <p>这里单独拆成 `void` 方法，是为了兼容 `RequestHeadersSpec` 与
     * `RequestBodySpec` 两条类型链，避免在 POST 请求里因为类型收窄而失去
     * `bodyValue(...)` 方法。
     */
    private void applyHeaders(
            WebClient.RequestHeadersSpec<?> requestSpec,
            Map<String, Object> input) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (document.defaults() != null && document.defaults().headers() != null) {
            headers.putAll(document.defaults().headers());
        }
        if (definition.headers() != null) {
            headers.putAll(definition.headers());
        }
        if (definition.request() != null && definition.request().headers() != null) {
            definition.request().headers().forEach((key, value) -> headers.put(key, applyTemplate(value, input)));
        }
        if (userId != null) {
            bindingService
                    .resolveHeaders(userId, sourceId)
                    .forEach(headers::put);
        }

        log.debug(
                "远程工具请求头概览: userId={}, username={}, sourceId={}, toolName={}, headerKeys={}",
                userId,
                username,
                sourceId,
                definition.name(),
                headers.keySet());
        headers.forEach(requestSpec::header);
        applyAuth(requestSpec);
    }

    /**
     * 为了让 POST 分支保持明确的返回类型，这里单独封装请求体写入逻辑。
     */
    private WebClient.RequestHeadersSpec<?> applyBody(
            WebClient.RequestBodySpec requestSpec,
            Map<String, Object> body) {
        return requestSpec.bodyValue(body);
    }

    /**
     * 根据认证配置注入请求头。
     *
     * <p>认证信息始终由服务端环境变量提供，不允许模型直接传入。
     */
    private void applyAuth(WebClient.RequestHeadersSpec<?> requestSpec) {
        RemoteToolAuth auth = definition.auth() != null ? definition.auth()
                : document.defaults() != null ? document.defaults().auth() : null;
        if (auth == null || auth.type() == null) {
            return;
        }

        String type = safeUpperCase(auth.type());
        if ("NONE".equals(type)) {
            return;
        }

        String token = auth.tokenEnv() != null ? System.getenv(auth.tokenEnv()) : null;
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("远程工具认证环境变量缺失: " + auth.tokenEnv());
        }

        switch (type) {
            case "BEARER" -> requestSpec.header("Authorization", "Bearer " + token);
            case "API_KEY" -> {
                String headerName = auth.headerName();
                if (headerName == null || headerName.isBlank()) {
                    throw new IllegalStateException("API_KEY 认证缺少 headerName 配置");
                }
                requestSpec.header(headerName, token);
            }
            default -> throw new IllegalStateException("暂不支持的远程工具认证类型: " + auth.type());
        }
    }

    /**
     * 打印一次远程请求的整体画像，帮助定位 URL、查询参数和请求体是否按预期组装。
     *
     * <p>这里刻意只打印字段名，不打印 header value 或 body 的具体业务值，避免日志泄露。
     */
    private void logRequestOverview(
            String method,
            String url,
            Map<String, String> queryParams,
            Map<String, Object> body,
            Map<String, Object> input) {
        log.info(
                "远程工具请求概览: userId={}, username={}, sourceId={}, toolName={}, method={}, url={}, queryKeys={}, bodyKeys={}, inputKeys={}",
                userId,
                username,
                sourceId,
                definition.name(),
                method,
                url,
                queryParams.keySet(),
                body.keySet(),
                input.keySet());
        logRawRequestIfEnabled(method, url, queryParams, body);
    }

    /**
     * 在显式开启调试开关时打印原始请求参数。
     *
     * <p>这里只打印 query 和 body；header 的明文值仍然不输出，避免把
     * token 或用户凭据写进日志。
     */
    private void logRawRequestIfEnabled(
            String method,
            String url,
            Map<String, String> queryParams,
            Map<String, Object> body) {
        if (!remoteToolLoggingProperties.isLogRawPayloads()) {
            return;
        }
        log.info(
                "远程工具原始请求: userId={}, username={}, sourceId={}, toolName={}, method={}, url={}, rawQuery={}, rawBody={}",
                userId,
                username,
                sourceId,
                definition.name(),
                method,
                url,
                truncatePayload(toJsonQuietly(queryParams)),
                truncatePayload(toJsonQuietly(body)));
    }

    /**
     * 在显式开启调试开关时打印原始响应体。
     */
    private void logRawResponseIfEnabled(
            String method,
            String url,
            String responseBody) {
        if (!remoteToolLoggingProperties.isLogRawPayloads()) {
            return;
        }
        log.info(
                "远程工具原始响应: userId={}, username={}, sourceId={}, toolName={}, method={}, url={}, rawResponse={}",
                userId,
                username,
                sourceId,
                definition.name(),
                method,
                url,
                truncatePayload(responseBody));
    }

    /**
     * 将对象安全序列化为 JSON 字符串。
     *
     * <p>序列化失败时退回到 {@code toString()}，确保日志流程本身不会阻断
     * 正常请求。
     */
    private String toJsonQuietly(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException error) {
            return String.valueOf(value);
        }
    }

    /**
     * 对原始 payload 日志做长度截断，避免大响应体撑爆日志系统。
     */
    private String truncatePayload(String payload) {
        if (payload == null) {
            return "null";
        }
        int maxLength = Math.max(remoteToolLoggingProperties.getMaxPayloadLength(), 256);
        if (payload.length() <= maxLength) {
            return payload;
        }
        return payload.substring(0, maxLength) + "...(truncated)";
    }

    /**
     * 提取工具结果的文本长度，避免在日志里输出工具的完整返回内容。
     */
    private int extractResultLength(ToolResultBlock result) {
        if (result == null || result.getOutput() == null || result.getOutput().isEmpty()) {
            return 0;
        }
        return result.getOutput().stream()
                .filter(TextBlock.class::isInstance)
                .map(TextBlock.class::cast)
                .map(TextBlock::getText)
                .filter(text -> text != null)
                .mapToInt(String::length)
                .sum();
    }

    /**
     * 组装绝对 URL。
     *
     * <p>路径参数使用简单模板替换，优先应用工具级 baseUrl，找不到再回退到
     * 文档默认 baseUrl。
     */
    private String buildAbsoluteUrl(Map<String, Object> input) {
        String baseUrl = definition.baseUrl() != null && !definition.baseUrl().isBlank()
                ? definition.baseUrl()
                : document.defaults() != null ? document.defaults().baseUrl() : null;
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("远程工具缺少 baseUrl 配置: " + definition.name());
        }

        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        String path = definition.path() != null ? definition.path() : "";
        String resolvedPath = applyPathParams(path, input);
        return normalizedBaseUrl + resolvedPath;
    }

    /**
     * 基于 `pathParams` 把模板变量替换进路径。
     */
    private String applyPathParams(String rawPath, Map<String, Object> input) {
        RemoteToolRequest request = definition.request();
        if (request == null || request.pathParams() == null || request.pathParams().isEmpty()) {
            return rawPath;
        }

        String path = rawPath;
        for (Map.Entry<String, String> entry : request.pathParams().entrySet()) {
            String token = "{{" + entry.getKey() + "}}";
            String value = applyTemplate(entry.getValue(), input);
            path = path.replace(token, value);
            path = path.replace(":" + entry.getKey(), value);
        }
        return path;
    }

    /**
     * 计算查询参数映射，并自动过滤空值。
     */
    private Map<String, String> resolveQueryParams(Map<String, Object> input) {
        RemoteToolRequest request = definition.request();
        if (request == null || request.queryParams() == null || request.queryParams().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> queryParams = new LinkedHashMap<>();
        request.queryParams().forEach((key, value) -> {
            String resolved = applyTemplate(value, input);
            if (resolved != null && !resolved.isBlank()) {
                queryParams.put(key, resolved);
            }
        });
        return queryParams;
    }

    /**
     * 根据 `bodyTemplate` 组装请求体。
     */
    private Map<String, Object> resolveBodyTemplate(Map<String, Object> input) {
        RemoteToolRequest request = definition.request();
        if (request == null || request.bodyTemplate() == null || request.bodyTemplate().isEmpty()) {
            return Collections.emptyMap();
        }
        return resolveObjectMap(request.bodyTemplate(), input);
    }

    /**
     * 把任意嵌套 Map 模板解析成最终对象。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveObjectMap(Map<String, Object> raw, Map<String, Object> input) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String stringValue) {
                resolved.put(entry.getKey(), resolveTemplateValue(stringValue, input));
            } else if (value instanceof Map<?, ?> nestedMap) {
                resolved.put(entry.getKey(), resolveObjectMap((Map<String, Object>) nestedMap, input));
            } else if (value instanceof List<?> list) {
                resolved.put(entry.getKey(), resolveList(list, input));
            } else {
                resolved.put(entry.getKey(), value);
            }
        }
        return resolved;
    }

    /**
     * 解析列表模板，允许列表元素继续嵌套 Map 或字符串模板。
     */
    @SuppressWarnings("unchecked")
    private List<Object> resolveList(List<?> rawList, Map<String, Object> input) {
        List<Object> resolved = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof String stringValue) {
                resolved.add(resolveTemplateValue(stringValue, input));
            } else if (item instanceof Map<?, ?> nestedMap) {
                resolved.add(resolveObjectMap((Map<String, Object>) nestedMap, input));
            } else if (item instanceof List<?> nestedList) {
                resolved.add(resolveList(nestedList, input));
            } else {
                resolved.add(item);
            }
        }
        return resolved;
    }

    /**
     * 解析请求体模板值。
     *
     * <p>与 header/query/path 这类“必须是字符串”的场景不同，请求体里的
     * 纯占位符需要尽量保留原始类型。例如：
     * <ul>
     *     <li>`{{frontImg}}` 且输入是 Map，则保留为对象</li>
     *     <li>`{{applyInfo}}` 且输入是 List，则保留为数组</li>
     *     <li>`{{pageNum}}` 且输入是整数，则保留为整数</li>
     * </ul>
     *
     * <p>当纯占位符缺值时，返回 {@code null}。这比统一降级为空字符串更稳，
     * 可以避免把对象、数组或数字字段错误地发成字符串类型。
     *
     * <p>只有在“字符串中包含占位符”的情况下，才退回字符串替换逻辑。
     */
    private Object resolveTemplateValue(String template, Map<String, Object> input) {
        Matcher matcher = TEMPLATE_TOKEN_PATTERN.matcher(template != null ? template : "");
        if (matcher.matches()) {
            String tokenKey = matcher.group(1);
            Object rawValue = input.get(tokenKey);
            if (!input.containsKey(tokenKey)) {
                log.warn(
                        "远程工具模板变量缺值，已降级为 null: userId={}, username={}, sourceId={}, toolName={}, tokenKey={}",
                        userId,
                        username,
                        sourceId,
                        definition.name(),
                        tokenKey);
                return null;
            }
            return rawValue;
        }
        return applyTemplate(template, input);
    }

    /**
     * 处理响应映射。
     *
     * <p>第一版只支持：
     * <ul>
     *     <li>`raw`：直接返回响应原文</li>
     *     <li>`json_path`：从 JSON 中提取 `$.a.b` 形式路径</li>
     * </ul>
     */
    private ToolResultBlock mapResponseToBlock(String rawBody) {
        RemoteToolResponse response = definition.response();
        if (response == null || response.mapping() == null || response.mapping().type() == null) {
            return textResult(rawBody);
        }

        String mappingType = safeUpperCase(response.mapping().type());
        return switch (mappingType) {
            case "RAW" -> textResult(rawBody);
            case "JSON_PATH" -> textResult(applyJsonPath(rawBody, response.mapping()));
            default -> throw new IllegalStateException("暂不支持的响应映射类型: " + response.mapping().type());
        };
    }

    /**
     * 支持最小子集的 JSON Path。
     *
     * <p>当前仅支持形如 `$.a.b.c` 的对象路径，不支持过滤表达式、
     * 数组切片等复杂语法。这样可以先满足常见读取场景，同时保持实现简单。
     */
    private String applyJsonPath(String rawBody, RemoteToolResponseMapping mapping) {
        if (mapping.path() == null || mapping.path().isBlank()) {
            return rawBody;
        }

        try {
            JsonNode node = objectMapper.readTree(rawBody);
            String path = mapping.path().trim();
            if (!path.startsWith("$.")) {
                throw new IllegalStateException("当前仅支持 '$.' 开头的 json_path");
            }

            String[] segments = path.substring(2).split("\\.");
            for (String segment : segments) {
                if (segment.isBlank()) {
                    continue;
                }
                node = node != null ? node.get(segment) : null;
            }
            if (node == null || node.isMissingNode() || node.isNull()) {
                return "";
            }
            if (node.isTextual()) {
                return node.asText();
            }
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("无法解析远程工具 JSON 响应", e);
        }
    }

    /**
     * 统一构建文本结果块，保持所有远程工具的返回格式一致。
     */
    private ToolResultBlock textResult(String text) {
        return ToolResultBlock.of(TextBlock.builder().text(text != null ? text : "").build());
    }

    /**
     * 构建最终请求 URI。
     */
    private java.net.URI buildUri(String absoluteUrl, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(absoluteUrl);
        queryParams.forEach(builder::queryParam);
        return builder.build(true).toUri();
    }

    /**
     * 计算工具超时时间。
     */
    private Duration resolveTimeout() {
        Integer timeoutSeconds = definition.timeoutSeconds() != null
                ? definition.timeoutSeconds()
                : document.defaults() != null ? document.defaults().timeoutSeconds() : null;
        int seconds = timeoutSeconds != null && timeoutSeconds > 0 ? timeoutSeconds : 10;
        return Duration.ofSeconds(seconds);
    }

    /**
     * 执行简单模板替换。
     *
     * <p>模板必须是 `{{field}}` 形式。当前不支持表达式和函数，避免让元数据
     * 格式过度膨胀。
     *
     * <p>该方法专门服务于“字符串上下文”，例如 query/header/path 或
     * `"prefix-{{field}}"` 这种场景，因此缺值时会替换为空串。
     */
    private String applyTemplate(String template, Map<String, Object> input) {
        if (template == null) {
            return null;
        }
        Matcher matcher = TEMPLATE_TOKEN_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String tokenKey = matcher.group(1);
            Object rawValue = input.get(tokenKey);
            if (!input.containsKey(tokenKey)) {
                // 缺值时不能把模板原样发给远程 API，否则会把 "{{field}}" 当成真实业务值。
                log.warn(
                        "远程工具字符串模板变量缺值，已降级为空字符串: userId={}, username={}, sourceId={}, toolName={}, tokenKey={}",
                        userId,
                        username,
                        sourceId,
                        definition.name(),
                        tokenKey);
            }
            matcher.appendReplacement(
                    buffer,
                    Matcher.quoteReplacement(stringify(rawValue)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 统一把模板值转成字符串。
     */
    private String stringify(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String string) {
            return string;
        }
        return String.valueOf(value);
    }

    /**
     * 统一转大写，避免到处散落空判断和 Locale 指定。
     */
    private String safeUpperCase(String value) {
        return value != null ? value.toUpperCase(Locale.ROOT) : "";
    }

    /**
     * 便于诊断来源。
     */
    @Override
    public String toString() {
        return "RemoteHttpAgentTool{" +
                "sourceId='" + sourceId + '\'' +
                ", toolName='" + definition.name() + '\'' +
                '}';
    }
}
