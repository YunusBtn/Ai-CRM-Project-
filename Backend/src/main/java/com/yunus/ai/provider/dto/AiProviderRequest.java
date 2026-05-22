package com.yunus.ai.provider.dto;

public record AiProviderRequest(

        String promt,
        String model,
        Integer maxOutputTokens


) {
}
