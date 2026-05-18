package xiaogj.xzagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 远程工具日志配置。
 *
 * <p>远程来源经常会携带业务敏感参数、响应体甚至用户凭据，因此原始入参
 * 与出参日志必须通过显式开关控制，不能默认开启。第一版仅提供最小可用
 * 的调试能力：
 * <ul>
 *     <li>{@code logRawPayloads}: 是否打印原始 query/body/response 文本。</li>
 *     <li>{@code maxPayloadLength}: 单条日志允许输出的最大文本长度。</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "xzagent.remote-tool.logging")
public class XzagentRemoteToolLoggingProperties {

    /**
     * 是否打印远程 HTTP 原始入参和出参。
     *
     * <p>默认关闭，避免把业务请求体和远程响应体长期写入日志。
     */
    private boolean logRawPayloads = false;

    /**
     * 单条原始 payload 日志的最大输出长度。
     *
     * <p>超出部分会被截断，并追加省略提示，避免大响应体冲垮日志系统。
     */
    private int maxPayloadLength = 4000;

    public boolean isLogRawPayloads() {
        return logRawPayloads;
    }

    public void setLogRawPayloads(boolean logRawPayloads) {
        this.logRawPayloads = logRawPayloads;
    }

    public int getMaxPayloadLength() {
        return maxPayloadLength;
    }

    public void setMaxPayloadLength(int maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }
}
