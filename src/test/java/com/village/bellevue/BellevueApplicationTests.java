package com.village.bellevue;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import com.village.bellevue.integration.IntegrationTestWrapper;

@SpringBootTest
class BellevueApplicationTests extends IntegrationTestWrapper {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {}

    @Test
    void testBeanInitialization() {
        assertThat(applicationContext.getBean("authenticationSuccessHandlerImpl")).isNotNull();
        assertThat(applicationContext.getBean("bellevueApplication")).isNotNull();
        assertThat(applicationContext.getBean("cacheConfig")).isNotNull();
        assertThat(applicationContext.getBean("cacheManager")).isNotNull();
        assertThat(applicationContext.getBean("filterChain")).isNotNull();
        assertThat(applicationContext.getBean("logoutSuccessHandlerImpl")).isNotNull();
        assertThat(applicationContext.getBean("passwordEncoder")).isNotNull();
        assertThat(applicationContext.getBean("redisTemplate")).isNotNull();
        assertThat(applicationContext.getBean("securityConfig")).isNotNull();
        assertThat(applicationContext.getBean("userDetailsServiceImpl")).isNotNull();

        assertThat(applicationContext.getBean("equipmentController")).isNotNull();
        assertThat(applicationContext.getBean("equipmentRepository")).isNotNull();
        assertThat(applicationContext.getBean("equipmentServiceImpl")).isNotNull();

        assertThat(applicationContext.getBean("friendController")).isNotNull();
        assertThat(applicationContext.getBean("friendRepository")).isNotNull();
        assertThat(applicationContext.getBean("friendServiceImpl")).isNotNull();

        assertThat(applicationContext.getBean("ingredientController")).isNotNull();
        assertThat(applicationContext.getBean("ingredientRepository")).isNotNull();
        assertThat(applicationContext.getBean("ingredientServiceImpl")).isNotNull();

        assertThat(applicationContext.getBean("recipeController")).isNotNull();
        assertThat(applicationContext.getBean("recipeRepository")).isNotNull();
        assertThat(applicationContext.getBean("recipeServiceImpl")).isNotNull();

        assertThat(applicationContext.getBean("reviewController")).isNotNull();
        assertThat(applicationContext.getBean("reviewRepository")).isNotNull();
        assertThat(applicationContext.getBean("reviewServiceImpl")).isNotNull();

        assertThat(applicationContext.getBean("skillController")).isNotNull();
        assertThat(applicationContext.getBean("skillRepository")).isNotNull();
        assertThat(applicationContext.getBean("skillServiceImpl")).isNotNull();

        assertThat(applicationContext.getBean("aggregateRatingRepository")).isNotNull();
        assertThat(applicationContext.getBean("scrubbedUserRepository")).isNotNull();
        assertThat(applicationContext.getBean("userRepository")).isNotNull();
    }

    @Test
    void testProperties() {
        String someProperty = applicationContext.getEnvironment().getProperty("spring.application.name");
        assertThat(someProperty).isEqualTo("bellevue");
    }

    @Test
    void testDatabaseConnection() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void testRedisConnection() {
        String key = "testKey";
        String value = "testValue";
        redisTemplate.opsForValue().set(key, value);
        String retrievedValue = (String) redisTemplate.opsForValue().get(key);
        assertThat(retrievedValue).isEqualTo(value);
        redisTemplate.delete(key);
    }
}
