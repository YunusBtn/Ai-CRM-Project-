package com.yunus.ai.dto;

public record AiProviderRequest(

        String promt,
        String model,
        Integer maxOutputTokens


) {
}
