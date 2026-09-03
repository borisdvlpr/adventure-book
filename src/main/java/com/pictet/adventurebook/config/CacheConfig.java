package com.pictet.adventurebook.config;

import com.pictet.adventurebook.model.dto.response.BookDetailsResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String BOOKS_CACHE = "books";
    private static final Duration BOOKS_TTL = Duration.ofHours(1);
    private static final String KEY_PREFIX = "adventurebook:";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(BOOKS_CACHE, typedCache(BookDetailsResponse.class, BOOKS_TTL))
                .disableCreateOnMissingCache()
                .transactionAware()
                .build();
    }

    private static RedisCacheConfiguration typedCache(Class<?> type, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> KEY_PREFIX + cacheName + "::")
                .serializeValuesWith(SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(type)));
    }
}
