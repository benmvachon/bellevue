package com.village.bellevue.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

class CacheConfigTest {

  @InjectMocks private CacheConfig cacheConfig;

  @Mock private RedisConnectionFactory redisConnectionFactory;

  private CacheManager cacheManager;
  private RedisTemplate<String, Object> redisTemplate;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCacheManager() {
    cacheManager = cacheConfig.cacheManager(redisConnectionFactory);

    assertThat(cacheManager).isNotNull();
    assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);

    assertThat(cacheConfig).isNotNull();
  }

  @Test
  void testRedisTemplate() {
    redisTemplate = cacheConfig.redisTemplate(redisConnectionFactory);

    assertThat(redisTemplate).isNotNull();
    assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
    assertThat(redisTemplate.getValueSerializer())
        .isInstanceOf(GenericJackson2JsonRedisSerializer.class);
    assertThat(redisTemplate.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
    assertThat(redisTemplate.getHashValueSerializer())
        .isInstanceOf(GenericJackson2JsonRedisSerializer.class);
  }

  @Test
  void testGetCacheKey() {
    String cacheKey = "testCache";
    Long userId = 1L;
    Long entityId = 2L;

    String expectedKey =
        cacheKey
            + CacheConfig.CACHE_KEY_SEPARATOR
            + userId
            + CacheConfig.CACHE_KEY_SEPARATOR
            + entityId;
    String actualKey = CacheConfig.getCacheKey(cacheKey, userId, entityId);

    assertThat(actualKey).isEqualTo(expectedKey);
  }

  @Test
  void testGetEntityCacheKeyPattern() {
    String cacheKey = "testCache";
    Long entityId = 2L;

    String expectedPattern =
        cacheKey
            + CacheConfig.CACHE_KEY_SEPARATOR
            + CacheConfig.CACHE_WILDCARD_MATCH
            + CacheConfig.CACHE_KEY_SEPARATOR
            + entityId;
    String actualPattern = CacheConfig.getEntityCacheKeyPattern(cacheKey, entityId);

    assertThat(actualPattern).isEqualTo(expectedPattern);
  }

  @Test
  void testGetUserCacheKeyPattern() {
    String cacheKey = "testCache";
    Long userId = 1L;

    String expectedPattern =
        cacheKey
            + CacheConfig.CACHE_KEY_SEPARATOR
            + userId
            + CacheConfig.CACHE_KEY_SEPARATOR
            + CacheConfig.CACHE_WILDCARD_MATCH;
    String actualPattern = CacheConfig.getUserCacheKeyPattern(cacheKey, userId);

    assertThat(actualPattern).isEqualTo(expectedPattern);
  }
}
