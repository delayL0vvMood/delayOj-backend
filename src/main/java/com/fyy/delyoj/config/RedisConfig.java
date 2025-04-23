package com.fyy.delyoj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    /**
     * 配置RedisTemplate
     * @param factory Redis连接工厂（自动注入）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 1. 设置连接工厂
        template.setConnectionFactory(factory);

        // 2. 配置序列化方式
        // Key的序列化（String类型）
        template.setKeySerializer(new StringRedisSerializer());

        // Hash Key的序列化
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value的序列化（JSON格式）
        Jackson2JsonRedisSerializer<Object> valueSerializer =
                new Jackson2JsonRedisSerializer<>(Object.class);
        template.setValueSerializer(valueSerializer);

        // Hash Value的序列化
        template.setHashValueSerializer(valueSerializer);

        // 3. 初始化配置
        template.afterPropertiesSet();
        return template;
    }
}
