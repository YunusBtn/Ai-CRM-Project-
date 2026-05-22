package com.yunus.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class OpenAiRestClientConfig {


    private final AiProperties aiProperties;

    @Bean
    public RestClient openAiRestClient() {


        //Connection timeout : Openai sunucusuna bağlanmak için max bekleme
        //Read timeout : Openai sunucusundan gelen cevap bekleme
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                .withReadTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()));


        //Timeout ayarlarına sahip request factory.
        var requestFactory = ClientHttpRequestFactories.get(settings);


        return RestClient.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + aiProperties.getApiKey())
                .requestFactory(requestFactory)
                .build();
    }














}
