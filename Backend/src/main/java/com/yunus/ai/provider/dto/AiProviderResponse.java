package com.yunus.ai.provider.dto;

public record AiProviderResponse(


        String content,
        Integer inputTokenEstimate,
        Integer outputTokenEstimate

) {
}
