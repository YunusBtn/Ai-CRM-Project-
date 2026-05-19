package com.yunus.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String provider;
    private String model;
    private String apiKey;
    private String baseUrl;
    private Integer timeoutSeconds;
    private Integer maxOutputTokens;
    private Integer maxContextMessages;
    private String promptVersion;
}