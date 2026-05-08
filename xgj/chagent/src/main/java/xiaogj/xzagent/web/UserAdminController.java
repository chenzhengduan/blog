package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import java.util.List;
import xiaogj.xzagent.service.UserAdminService;
import xiaogj.xzagent.web.dto.AuthUserResponse;
import xiaogj.xzagent.web.dto.UserUpsertRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 超管用户管理接口。
 */
@RestController
@RequestMapping("/xzagent/api/admin/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    /**
     * 列出全部用户。
     */
    @GetMapping
    public List<AuthUserResponse> listUsers() {
        return userAdminService.listUsers();
    }

    /**
     * 新增用户。
     */
    @PostMapping
    public AuthUserResponse createUser(@Valid @RequestBody UserUpsertRequest request) {
        return userAdminService.createUser(request);
    }

    /**
     * 删除用户。
     */
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userAdminService.deleteUser(userId);
    }
}
