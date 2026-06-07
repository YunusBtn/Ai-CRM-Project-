package com.yunus.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
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
import java.util.HashMap;
import java.util.Map;

/*
 * RedisConfig sınıfının amacı:
 *
 * Bu sınıf Redis bağlantısını, RedisTemplate kullanımını
 * ve Spring Cache sisteminin Redis üzerinden çalışmasını yapılandırır.
 *
 * Bu yapı sayesinde:
 * - Redis'e manuel veri yazıp okuyabiliriz.
 * - @Cacheable, @CacheEvict, @CachePut anotasyonlarını kullanabiliriz.
 * - Cache verilerini JSON olarak saklayabiliriz.
 * - Cache key'lerine proje bazlı prefix ekleyebiliriz.
 */
@Configuration

// Spring Cache anotasyonlarını aktif eder.
// Bu olmazsa @Cacheable, @CacheEvict, @CachePut gibi anotasyonlar çalışmaz.
@EnableCaching

// final alanlar için constructor oluşturur.
// RedisProperties constructor üzerinden inject edilir.
@RequiredArgsConstructor
public class RedisConfig {

    /*
     * application.properties veya application.yml içindeki:
     *
     * spring.data.redis.host
     * spring.data.redis.port
     * spring.data.redis.password
     * spring.data.redis.timeout
     *
     * gibi ayarları temsil eder.
     *
     * Böylece host, port gibi değerleri kod içine hardcoded yazmayız.
     */
    private final RedisProperties redisProperties;

    /*
     * Redis bağlantısını kuran ana bean'dir.
     *
     * Spring uygulaması Redis'e bağlanırken bu factory'yi kullanır.
     * Burada standalone Redis yapısı kullanılıyor.
     *
     * Yani:
     * - Redis Cluster yok
     * - Redis Sentinel yok
     * - Tek Redis instance var
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        // Redis sunucu bilgileri burada tanımlanır.
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();

        // Redis host bilgisini properties dosyasından alır.
        serverConfig.setHostName(redisProperties.getHost());

        // Redis port bilgisini properties dosyasından alır.
        serverConfig.setPort(redisProperties.getPort());

        // Redis şifresi varsa bağlantıya eklenir.
        // Local ortamda genelde boş olur, production'da çoğunlukla dolu olur.
        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            serverConfig.setPassword(redisProperties.getPassword());
        }

        // Lettuce client ayarları burada yapılır.
        // Lettuce, Spring Boot'un Redis için varsayılan client'ıdır.
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()

                // Redis komutları bu süre içinde cevap vermezse timeout oluşur.
                .commandTimeout(redisProperties.getTimeout())

                // Client configuration nesnesini oluşturur.
                .build();

        // Redis bağlantı factory'si oluşturulur.
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    /*
     * RedisTemplate manuel Redis işlemleri için kullanılır.
     *
     * Örnek kullanım:
     *
     * redisTemplate.opsForValue().set("product:1", productDto);
     * redisTemplate.opsForValue().get("product:1");
     *
     * Bu yapı @Cacheable'dan bağımsızdır.
     * Yani Redis'i doğrudan elle kullanmak istediğimizde gerekir.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            LettuceConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        // Redis key'lerinin düz String olarak saklanmasını sağlar.
        // Böylece Redis CLI veya GUI araçlarında key'ler okunabilir olur.
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Redis value'larının JSON olarak saklanmasını sağlayan serializer.
        GenericJackson2JsonRedisSerializer jsonSerializer = buildJsonSerializer(objectMapper);

        // String key, Object value tutan RedisTemplate oluşturulur.
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // RedisTemplate'in hangi Redis bağlantısını kullanacağı belirlenir.
        template.setConnectionFactory(connectionFactory);

        // Normal key'ler String olarak saklanır.
        template.setKeySerializer(stringSerializer);

        // Hash key'leri de String olarak saklanır.
        template.setHashKeySerializer(stringSerializer);

        // Normal value'lar JSON olarak saklanır.
        template.setValueSerializer(jsonSerializer);

        // Hash value'lar da JSON olarak saklanır.
        template.setHashValueSerializer(jsonSerializer);

        // Tüm ayarlar yapıldıktan sonra template initialize edilir.
        template.afterPropertiesSet();

        return template;
    }

    /*
     * RedisCacheManager, Spring Cache sisteminin Redis kullanmasını sağlar.
     *
     * Yani şu anotasyonların Redis üzerinde çalışmasını sağlar:
     *
     * @Cacheable
     * @CacheEvict
     * @CachePut
     *
     * RedisTemplate manuel işlemler içindir.
     * RedisCacheManager ise Spring Cache anotasyonları içindir.
     */
    @Bean
    public RedisCacheManager redisCacheManager(
            LettuceConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        // Cache key'leri String olarak saklanır.
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Cache value'ları JSON olarak saklanır.
        GenericJackson2JsonRedisSerializer jsonSerializer = buildJsonSerializer(objectMapper);

        /*
         * Varsayılan cache ayarları.
         *
         * Buradaki ayarlar, özel olarak belirtilmeyen tüm cache'ler için geçerlidir.
         */
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()

                // Cache'e yazılan veriler 30 dakika sonra otomatik silinir.
                .entryTtl(Duration.ofMinutes(30))

                // null değerlerin cache'e yazılmasını engeller.
                // Gereksiz cache kayıtlarını ve bazı hatalı cache senaryolarını önler.
                .disableCachingNullValues()

                // Cache key'lerinin String serializer ile saklanmasını sağlar.
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer)
                )

                // Cache value'larının JSON serializer ile saklanmasını sağlar.
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )

                // Redis key'lerinin başına proje bazlı prefix ekler.
                // Aynı Redis'i birden fazla uygulama kullanıyorsa key çakışmasını önler.
                .prefixCacheNameWith("appointment-api:");

        /*
         * Cache bazlı özel TTL ayarları burada tutulur.
         *
         * Örneğin bazı cache'ler 5 dakika,
         * bazıları 6 saat,
         * bazıları 1 gün tutulabilir.
         */
        Map<String, RedisCacheConfiguration> perCacheConfigs = new HashMap<>();

        // Projeye göre özel TTL tanımlamaları burada yapılabilir.
        // Örnek:
        // perCacheConfigs.put("products", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        // perCacheConfigs.put("categories", defaultCacheConfig.entryTtl(Duration.ofHours(6)));
        // perCacheConfigs.put("businesses", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));

        // RedisCacheManager oluşturulur.
        return RedisCacheManager.builder(connectionFactory)

                // Varsayılan cache ayarları uygulanır.
                .cacheDefaults(defaultCacheConfig)

                // Cache adına göre özel TTL ayarları eklenir.
                .withInitialCacheConfigurations(perCacheConfigs)

                // CacheManager bean'i oluşturulur.
                .build();
    }

    /*
     * Redis için JSON serializer üretir.
     *
     * Bu metodu ayrı yazmamızın sebebi:
     *
     * Hem RedisTemplate hem RedisCacheManager aynı serializer yapısını kullanıyor.
     * Kod tekrarını önlemek için ortak metoda çekildi.
     */
    private GenericJackson2JsonRedisSerializer buildJsonSerializer(ObjectMapper objectMapper) {

        // Ana ObjectMapper'ı doğrudan değiştirmiyoruz.
        // Redis'e özel ayrı bir kopya oluşturuyoruz.
        ObjectMapper redisObjectMapper = objectMapper.copy();

        /*
         * Object tipindeki veriler Redis'e yazılırken class bilgisi JSON içine eklenir.
         *
         * Böylece Redis'ten geri okunurken verinin hangi Java sınıfına dönüştürüleceği bilinir.
         */
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Redis value'larını JSON formatında serialize/deserialize eden serializer döndürülür.
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }
}
