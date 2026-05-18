package xiaogj.xzagent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * 提供统一的 Jackson {@link ObjectMapper}。
     *
     * <p>这里显式注册 Java 时间模块，避免会话状态、审计记录等对象
     * 在序列化和反序列化时出现时间字段兼容性问题。
     */
    @Bean
    public ObjectMapper jegentObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
