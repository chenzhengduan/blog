package xiaogj.xzagent.web;

import xiaogj.xzagent.service.ChatApplicationService;
import xiaogj.xzagent.web.dto.SessionResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会话状态查询与重置接口。
 */
@RestController
@RequestMapping("/xzagent/api/sessions")
public class SessionController {

    private final ChatApplicationService chatApplicationService;

    public SessionController(ChatApplicationService chatApplicationService) {
        this.chatApplicationService = chatApplicationService;
    }

    /**
     * 查询会话快照。
     */
    @GetMapping("/{sessionId}")
    public SessionResponse getSession(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatApplicationService.getSession(sessionId, page, size);
    }

    /**
     * 重置会话状态。
     */
    @DeleteMapping("/{sessionId}")
    public void resetSession(@PathVariable String sessionId) {
        chatApplicationService.resetSession(sessionId);
    }
}
