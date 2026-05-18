package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import java.util.List;
import xiaogj.xzagent.service.ThirdPartyCredentialService;
import xiaogj.xzagent.service.UserPrincipal;
import xiaogj.xzagent.web.dto.ThirdPartyCredentialCreateRequest;
import xiaogj.xzagent.web.dto.ThirdPartyCredentialResponse;
import xiaogj.xzagent.web.dto.ThirdPartyCredentialUpdateRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户自己的第三方凭证接口。
 */
@RestController
@RequestMapping("/xzagent/api/me/third-party-credentials")
public class MyThirdPartyCredentialController {

    private final ThirdPartyCredentialService thirdPartyCredentialService;

    public MyThirdPartyCredentialController(ThirdPartyCredentialService thirdPartyCredentialService) {
        this.thirdPartyCredentialService = thirdPartyCredentialService;
    }

    /**
     * 列出当前用户的全部凭证。
     */
    @GetMapping
    public List<ThirdPartyCredentialResponse> listCredentials(
            @AuthenticationPrincipal UserPrincipal principal) {
        return thirdPartyCredentialService.listCredentials(principal.userId());
    }

    /**
     * 创建当前用户的新凭证。
     */
    @PostMapping
    public ThirdPartyCredentialResponse createCredential(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ThirdPartyCredentialCreateRequest request) {
        return thirdPartyCredentialService.createCredential(principal.userId(), request);
    }

    /**
     * 更新当前用户的某个凭证。
     */
    @PutMapping("/{credentialId}")
    public ThirdPartyCredentialResponse updateCredential(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String credentialId,
            @Valid @RequestBody ThirdPartyCredentialUpdateRequest request) {
        return thirdPartyCredentialService.updateCredential(principal.userId(), credentialId, request);
    }

    /**
     * 删除当前用户的某个凭证。
     */
    @DeleteMapping("/{credentialId}")
    public void deleteCredential(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String credentialId) {
        thirdPartyCredentialService.deleteCredential(principal.userId(), credentialId);
    }
}
