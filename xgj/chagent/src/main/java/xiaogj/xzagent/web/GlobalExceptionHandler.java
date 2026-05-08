package xiaogj.xzagent.web;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import xiaogj.xzagent.web.dto.ApiErrorResponse;

/**
 * 全局异常处理器。
 *
 * <p>该处理器专门把常见的参数校验、请求体解析和业务参数错误转换成结构化响应，
 * 避免前端只能拿到空洞的 `400 Bad Request`。同时这里会统一打印关键日志，
 * 让后端排查时能直接看到失败原因，而不是只看到路径和状态码。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理 `@Valid @RequestBody` 触发的字段校验失败。
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(
            WebExchangeBindException exception,
            ServerWebExchange exchange) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        log.warn("请求参数校验失败: path={}, details={}",
                exchange.getRequest().getPath().value(),
                details);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "请求参数校验失败",
                details,
                exchange);
    }

    /**
     * 处理 WebFlux 请求体或请求参数解析失败。
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiErrorResponse> handleServerWebInputException(
            ServerWebInputException exception,
            ServerWebExchange exchange) {
        log.warn("请求输入解析失败: path={}, message={}",
                exchange.getRequest().getPath().value(),
                exception.getReason(),
                exception);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getReason() != null ? exception.getReason() : "请求输入解析失败",
                List.of(),
                exchange);
    }

    /**
     * 处理方法级参数校验异常。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            ServerWebExchange exchange) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        log.warn("约束校验失败: path={}, details={}",
                exchange.getRequest().getPath().value(),
                details);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "请求约束校验失败",
                details,
                exchange);
    }

    /**
     * 处理明确的业务参数异常。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            ServerWebExchange exchange) {
        log.warn("业务参数异常: path={}, message={}",
                exchange.getRequest().getPath().value(),
                exception.getMessage(),
                exception);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage() != null ? exception.getMessage() : "请求参数不合法",
                List.of(),
                exchange);
    }

    /**
     * 处理显式抛出的状态异常。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            ServerWebExchange exchange) {
        log.warn("状态异常: path={}, status={}, reason={}",
                exchange.getRequest().getPath().value(),
                exception.getStatusCode(),
                exception.getReason(),
                exception);
        return buildResponse(
                HttpStatus.valueOf(exception.getStatusCode().value()),
                exception.getReason() != null ? exception.getReason() : "请求处理失败",
                List.of(),
                exchange);
    }

    /**
     * 兜底处理未预期异常。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception exception,
            ServerWebExchange exchange) {
        log.error("未处理异常: path={}", exchange.getRequest().getPath().value(), exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "服务内部异常，请查看后端日志定位具体原因",
                List.of(),
                exchange);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            List<String> details,
            ServerWebExchange exchange) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                exchange.getRequest().getPath().value(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getId(),
                details);
        return ResponseEntity.status(status).body(response);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
