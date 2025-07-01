package kr.hhplus.be.server.config.cache;

import java.util.HashMap;
import java.util.Map;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient, CacheProperties cacheProperties) {
        Map<String,CacheConfig> configMap = new HashMap();

        if (cacheProperties.getConfigs() != null) {
            cacheProperties.getConfigs().forEach((cacheName, detail) -> { // application.yml 에서 등록
                long ttl = detail.getTtl();
                long maxIdleTime = detail.getMaxIdleTime();
                configMap.put(cacheName, new CacheConfig(ttl, maxIdleTime));
            });
        }

        return new RedissonSpringCacheManager(redissonClient, configMap);

    }
}
