package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import java.util.List;
import xiaogj.xzagent.service.UserPrincipal;
import xiaogj.xzagent.service.UserRemoteSourceCredentialBindingService;
import xiaogj.xzagent.web.dto.UserRemoteSourceCredentialBindingResponse;
import xiaogj.xzagent.web.dto.UserRemoteSourceCredentialBindingUpdateRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户自己的远程来源凭证绑定接口。
 */
@RestController
@RequestMapping("/xzagent/api/me/remote-source-credential-bindings")
public class MyRemoteSourceCredentialBindingController {

    private final UserRemoteSourceCredentialBindingService bindingService;

    public MyRemoteSourceCredentialBindingController(
            UserRemoteSourceCredentialBindingService bindingService) {
        this.bindingService = bindingService;
    }

    /**
     * 列出当前用户的全部来源绑定。
     */
    @GetMapping
    public List<UserRemoteSourceCredentialBindingResponse> listBindings(
            @AuthenticationPrincipal UserPrincipal principal) {
        return bindingService.listBindings(principal.userId());
    }

    /**
     * 保存当前用户对某个来源的绑定。
     */
    @PutMapping("/{sourceId}")
    public UserRemoteSourceCredentialBindingResponse saveBinding(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String sourceId,
            @Valid @RequestBody UserRemoteSourceCredentialBindingUpdateRequest request) {
        return bindingService.saveBinding(principal.userId(), sourceId, request);
    }

    /**
     * 删除当前用户对某个来源的绑定。
     */
    @DeleteMapping("/{sourceId}")
    public void deleteBinding(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String sourceId) {
        bindingService.deleteBinding(principal.userId(), sourceId);
    }
}
