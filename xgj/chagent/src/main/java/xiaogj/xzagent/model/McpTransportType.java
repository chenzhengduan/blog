package xiaogj.xzagent.model;

public enum McpTransportType {
    /** 通过本地进程标准输入输出与 MCP Server 通信。 */
    STDIO,

    /** 通过 SSE 长连接消费服务端推送。 */
    SSE,

    /** 通过可流式 HTTP 协议访问 MCP Server。 */
    HTTP
}
