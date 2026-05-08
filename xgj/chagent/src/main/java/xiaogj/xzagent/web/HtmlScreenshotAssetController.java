package xiaogj.xzagent.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import xiaogj.xzagent.tool.HtmlScreenshotTool;
import xiaogj.xzagent.tool.HtmlScreenshotConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTML 截图文件访问控制器。
 *
 * <p>该控制器只负责暴露截图结果，不参与截图生成。这样工具可以返回一个前端或
 * 其他系统可继续访问的 URL，而不是只能返回容器内绝对路径。
 */
@RestController
@RequestMapping(HtmlScreenshotConstants.PUBLIC_PATH_PREFIX)
@ConditionalOnProperty(prefix = "xzagent.tools.html-screenshot", name = "enabled", havingValue = "true")
public class HtmlScreenshotAssetController {

    private static final Pattern SAFE_FILE_NAME =
            Pattern.compile("^[A-Za-z0-9._-]+\\.png$");

    /**
     * 读取指定截图文件。
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getScreenshot(@PathVariable String fileName) {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            return ResponseEntity.badRequest().build();
        }
        Path file = HtmlScreenshotTool.outputDirectory().resolve(fileName);
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(file));
    }
}
