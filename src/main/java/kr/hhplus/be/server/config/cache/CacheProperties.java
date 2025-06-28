package kr.hhplus.be.server.config.cache;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "custom.cache")
public class CacheProperties {

    private Map<String, CacheDetail> configs;

    @Getter
    @Setter
    public static class CacheDetail {
        private long ttl;
        private long maxIdleTime;
    }

}
