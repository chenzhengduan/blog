package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import xiaogj.xzagent.service.UserApiKeyService;
import xiaogj.xzagent.service.UserPrincipal;
import xiaogj.xzagent.web.dto.UserApiKeyCreateRequest;
import xiaogj.xzagent.web.dto.UserApiKeyResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户 API Key 管理接口。
 */
@RestController
@RequestMapping("/xzagent/api/me/api-key")
public class MyApiKeyController {

    private final UserApiKeyService userApiKeyService;

    public MyApiKeyController(UserApiKeyService userApiKeyService) {
        this.userApiKeyService = userApiKeyService;
    }

    /**
     * 获取当前用户 API Key 状态。
     */
    @GetMapping
    public UserApiKeyResponse getCurrentApiKey(
            @AuthenticationPrincipal UserPrincipal principal) {
        return userApiKeyService.getCurrentUserApiKey(principal.userId());
    }

    /**
     * 创建或重置当前用户 API Key。
     */
    @PostMapping
    public UserApiKeyResponse createOrRotateApiKey(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserApiKeyCreateRequest request) {
        return userApiKeyService.createOrRotateCurrentUserApiKey(principal.userId(), request);
    }

    /**
     * 删除当前用户 API Key。
     */
    @DeleteMapping
    public void deleteCurrentApiKey(@AuthenticationPrincipal UserPrincipal principal) {
        userApiKeyService.deleteCurrentUserApiKey(principal.userId());
    }
}
