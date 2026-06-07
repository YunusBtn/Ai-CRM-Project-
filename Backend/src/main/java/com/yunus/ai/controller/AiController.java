package com.yunus.ai.controller;

import com.yunus.ai.dto.AiResultResponse;
import com.yunus.ai.service.AiService;
import com.yunus.common.ApiResponse;
import com.yunus.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/conversations/{conversationId}/ai/reply-suggestion")
    public ApiResponse<AiResultResponse> generateReplySuggestion(
            @PathVariable UUID conversationId
    ) {
        // Belirli bir conversation için temsilciye cevap önerisi üretir.
        return ApiResponse.success(aiService.generateReplySuggestion(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/ai/summary")
    public ApiResponse<AiResultResponse> generateSummary(
            @PathVariable UUID conversationId
    ) {
        // Belirli bir conversation için konuşma özeti üretir.
        return ApiResponse.success(aiService.generateSummary(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/ai/classification")
    public ApiResponse<AiResultResponse> classifyConversation(
            @PathVariable UUID conversationId
    ) {
        // Belirli bir conversation için intent, sentiment, priority gibi analiz sonucu üretir.
        return ApiResponse.success(aiService.classifyConversation(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/ai/tag-suggestion")
    public ApiResponse<AiResultResponse> suggestTags(
            @PathVariable UUID conversationId
    ) {
        // Belirli bir conversation için AI destekli tag önerileri üretir.
        return ApiResponse.success(aiService.suggestTags(conversationId));
    }

    @GetMapping("/conversations/{conversationId}/ai-results")
    public ApiResponse<PageResponse<AiResultResponse>> getConversationAiResults(
            @PathVariable UUID conversationId,
            Pageable pageable
    ) {
        // Belirli bir conversation'a ait AI result geçmişini sayfalı şekilde getirir.
        return ApiResponse.success(aiService.getConversationAiResults(conversationId, pageable));
    }

    @GetMapping("/ai-results/{id}")
    public ApiResponse<AiResultResponse> getAiResultById(
            @PathVariable UUID id
    ) {
        // Tek bir AI result kaydını id üzerinden getirir.
        return ApiResponse.success(aiService.getById(id));
    }
}
