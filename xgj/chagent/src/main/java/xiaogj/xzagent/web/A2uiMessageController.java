package xiaogj.xzagent.web;

import jakarta.validation.Valid;
import java.util.Map;
import xiaogj.xzagent.service.A2uiMessageApplicationService;
import xiaogj.xzagent.web.dto.A2uiMessageAckResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * A2UI client message 接收接口。
 */
@RestController
@RequestMapping("/xzagent/api/a2ui/messages")
public class A2uiMessageController {

    private final A2uiMessageApplicationService a2uiMessageApplicationService;

    public A2uiMessageController(A2uiMessageApplicationService a2uiMessageApplicationService) {
        this.a2uiMessageApplicationService = a2uiMessageApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public A2uiMessageAckResponse acceptMessage(
            @RequestParam("sessionId") String sessionId,
            @Valid @RequestBody Map<String, Object> payload) {
        a2uiMessageApplicationService.acceptClientMessage(sessionId, payload);
        return new A2uiMessageAckResponse(true);
    }
}
