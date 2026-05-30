package com.yunus.config;

/*
 * Bu sınıfın genel amacı:
 *
 * Spring Boot uygulamasının Redis ile nasıl bağlantı kuracağını,
 * Redis'e veri yazarken/okurken verilerin nasıl serialize edileceğini
 * ve @Cacheable / @CacheEvict gibi Spring Cache anotasyonlarının
 * Redis üzerinde nasıl çalışacağını yapılandırmaktır.
 *
 * Kısaca:
 * - RedisConnectionFactory: Redis bağlantısını kurar.
 * - RedisTemplate: Redis'e manuel veri yazıp okumayı sağlar.
 * - RedisCacheManager: Spring Cache sisteminin Redis'i kullanmasını sağlar.
 */

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

// Bu sınıfın Spring tarafından configuration sınıfı olarak algılanmasını sağlar.
@Configuration

// final alanlar için constructor oluşturur.
// Burada RedisProperties constructor ile inject edilir.
@RequiredArgsConstructor
public class RedisConfig {

    // application.properties içindeki spring.data.redis.* ayarlarını temsil eder.
    // Örneğin:
    // spring.data.redis.host
    // spring.data.redis.port
    // spring.data.redis.password
    // spring.data.redis.timeout
    private final RedisProperties redisProperties;

    // Redis'e bağlantı kuracak ana bağlantı factory bean'idir.
    // Spring uygulaması Redis'e bu factory üzerinden bağlanır.
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        // Tek bir Redis sunucusuna bağlanmak için kullanılır.
        // Cluster veya Sentinel değil, standalone Redis yapısıdır.
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();

        // Redis host bilgisini application.properties üzerinden alır.
        serverConfig.setHostName(redisProperties.getHost());

        // Redis port bilgisini application.properties üzerinden alır.
        serverConfig.setPort(redisProperties.getPort());

        // Redis şifresi varsa bağlantı ayarına ekler.
        // Local geliştirme ortamında genelde boş olabilir.
        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            serverConfig.setPassword(redisProperties.getPassword());
        }

        // Lettuce client ayarlarını oluşturur.
        // Burada Redis komutları için timeout belirlenir.
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()

                // Redis komutları belirlenen süre içinde cevap vermezse timeout olur.
                .commandTimeout(redisProperties.getTimeout())

                // Client configuration nesnesini oluşturur.
                .build();

        // Redis bağlantı factory'sini oluşturur.
        // serverConfig -> host, port, password
        // clientConfig -> timeout gibi client ayarları
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    // RedisTemplate, Redis'e manuel veri yazıp okumak için kullanılır.
    // Örneğin:
    // redisTemplate.opsForValue().set("key", value);
    // redisTemplate.opsForValue().get("key");
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            LettuceConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {

        // String key, Object value tutacak RedisTemplate oluşturulur.
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // RedisTemplate'in Redis bağlantısı için hangi connection factory'yi kullanacağı belirlenir.
        template.setConnectionFactory(connectionFactory);

        // Redis key'lerinin düz String olarak saklanmasını sağlar.
        // Böylece Redis CLI'da key'ler okunabilir görünür.
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Uygulamanın mevcut ObjectMapper'ının kopyasını alır.
        // Ana ObjectMapper'ı bozmayız, Redis'e özel mapper üretiriz.
        ObjectMapper redisObjectMapper = objectMapper.copy();

        // Redis'e Object tipinde veri yazarken class bilgisini JSON içine ekler.
        // Böylece Redis'ten geri okunurken hangi sınıfa dönüştürüleceği anlaşılır.
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Value'ların JSON formatında serialize/deserialize edilmesini sağlar.
        // Yani Object veriler Redis'te JSON olarak saklanır.
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Normal Redis key'leri String olarak saklanır.
        template.setKeySerializer(stringSerializer);

        // Hash key'leri de String olarak saklanır.
        template.setHashKeySerializer(stringSerializer);

        // Normal value'lar JSON olarak saklanır.
        template.setValueSerializer(jsonSerializer);

        // Hash value'lar da JSON olarak saklanır.
        template.setHashValueSerializer(jsonSerializer);

        // Template'in tüm ayarları yapıldıktan sonra initialize edilmesini sağlar.
        template.afterPropertiesSet();

        // Hazır RedisTemplate bean olarak Spring container'a verilir.
        return template;
    }

    // RedisCacheManager, Spring Cache anotasyonlarının Redis kullanmasını sağlar.
    // Örneğin:
    // @Cacheable
    // @CacheEvict
    // @CachePut
    @Bean
    public RedisCacheManager redisCacheManager(
            LettuceConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {

        // Cache key'lerinin String olarak saklanması için serializer.
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Redis cache value'ları için ObjectMapper kopyası alınır.
        ObjectMapper redisObjectMapper = objectMapper.copy();

        // Cache'e yazılan Object değerlerin class bilgisini JSON içinde tutar.
        // Bu sayede Redis'ten geri okurken doğru tipe çevrilebilir.
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Cache value'larının JSON formatında saklanmasını sağlar.
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Redis cache için genel ayarlar burada yapılır.
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()

                // Cache'e yazılan veriler 30 dakika sonra otomatik silinir.
                .entryTtl(Duration.ofMinutes(30))

                // null değerlerin cache'e yazılmasını engeller.
                // Böylece gereksiz ve hatalı cache kayıtları oluşmaz.
                .disableCachingNullValues()

                // Cache key'lerini String serializer ile saklar.
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer)
                )

                // Cache value'larını JSON serializer ile saklar.
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )

                // Tüm cache key'lerinin başına prefix ekler.
                // Redis içinde bu uygulamaya ait key'leri ayırmayı kolaylaştırır.
                .prefixCacheNameWith("appointment-api:");

        // RedisCacheManager oluşturulur.
        return RedisCacheManager.builder(connectionFactory)

                // Yukarıda tanımlanan default cache ayarları uygulanır.
                .cacheDefaults(cacheConfig)

                // CacheManager nesnesi build edilir.
                .build();
    }
}