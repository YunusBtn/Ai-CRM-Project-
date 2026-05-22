package com.yunus.ai.dto;

import com.yunus.ai.enums.AiResultStatus;
import com.yunus.ai.enums.AiResultType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AiResultResponse(
        UUID id,
        UUID conversationId,
        UUID customerId,
        UUID requestedById,
        String requestedByFullName,
        AiResultType type,
        AiResultStatus status,
        String model,
        String promptVersion,
        String inputSnapshot,
        String content,
        String errorMessage,
        Integer inputTokenEstimate,
        Integer outputTokenEstimate,
        Integer maxOutputTokens,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}