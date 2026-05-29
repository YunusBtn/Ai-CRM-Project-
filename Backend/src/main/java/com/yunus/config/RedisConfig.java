package com.yunus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration();

        // application.properties içinden host alır
        configuration.setHostName(
                redisProperties.getHost()
        );

        // application.properties içinden port alır
        configuration.setPort(
                redisProperties.getPort()
        );

        // Password varsa ekler
        if (redisProperties.getPassword() != null &&
                !redisProperties.getPassword().isBlank()) {

            configuration.setPassword(
                    RedisPassword.of(
                            redisProperties.getPassword()
                    )
            );
        }

        LettuceClientConfiguration lettuceConfig =
                LettuceClientConfiguration.builder()

                        // Redis timeout süresi
                        .commandTimeout(Duration.ofSeconds(2))

                        .build();

        return new LettuceConnectionFactory(
                configuration,
                lettuceConfig
        );
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory
    ) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Redis keyleri String olarak saklanır
        template.setKeySerializer(
                new StringRedisSerializer()
        );

        // Redis value'ları JSON olarak saklanır
        template.setValueSerializer(
                new GenericJackson2JsonRedisSerializer()
        );

        // Hash key serializer
        template.setHashKeySerializer(
                new StringRedisSerializer()
        );

        // Hash value serializer
        template.setHashValueSerializer(
                new GenericJackson2JsonRedisSerializer()
        );

        template.afterPropertiesSet();

        return template;
    }
}