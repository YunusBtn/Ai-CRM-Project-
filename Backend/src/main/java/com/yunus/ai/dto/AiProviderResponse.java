package com.yunus.ai.dto;

public record AiProviderResponse(


        String content,
        Integer inputTokenEstimate,
        Integer outputTokenEstimate

) {
}
