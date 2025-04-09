package com.village.bellevue.config;

import java.time.Duration;
import java.util.Set;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String CACHE_KEY_SEPARATOR = "_";
  public static final String CACHE_WILDCARD_MATCH = "*";

  public static final String FRIENDSHIP_STATUS_CACHE_NAME = "friendshipStatusCache";
  public static final String POST_SECURITY_CACHE_NAME = "postSecurityCache";

  public static void evictKeysByPattern(
      RedisTemplate<String, Object> redisTemplate, String cacheName, String pattern) {
    Set<String> keys = redisTemplate.keys(cacheName + ":" + pattern);
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  public static String getCacheKey(String cacheKey, Long user, Long entityId) {
    return cacheKey + CACHE_KEY_SEPARATOR + user + CACHE_KEY_SEPARATOR + entityId;
  }

  public static String getEntityCacheKeyPattern(String cacheKey, Long entityId) {
    return cacheKey + CACHE_KEY_SEPARATOR + CACHE_WILDCARD_MATCH + CACHE_KEY_SEPARATOR + entityId;
  }

  public static String getUserCacheKeyPattern(String cacheKey, Long user) {
    return cacheKey + CACHE_KEY_SEPARATOR + user + CACHE_KEY_SEPARATOR + CACHE_WILDCARD_MATCH;
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()))
            .entryTtl(Duration.ofHours(1));

    return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config).build();
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    return template;
  }
}
