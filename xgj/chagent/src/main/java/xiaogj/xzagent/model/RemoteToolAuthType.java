package xiaogj.xzagent.model;

/**
 * 远程工具元数据支持的认证类型。
 *
 * <p>第一版只支持最常见、最容易受控的几类认证。复杂认证流程
 * 例如 OAuth 授权码交换、签名算法、短时票据刷新等，暂不纳入
 * 轻量元数据标准，避免把运行时配置演变成脚本语言。
 */
public enum RemoteToolAuthType {
    NONE,
    BEARER,
    API_KEY
}

