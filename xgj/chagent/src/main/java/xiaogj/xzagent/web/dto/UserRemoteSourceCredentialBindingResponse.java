package xiaogj.xzagent.web.dto;

/**
 * 用户级远程来源凭证绑定响应。
 */
public record UserRemoteSourceCredentialBindingResponse(
        String sourceId,
        String credentialId) {
}
