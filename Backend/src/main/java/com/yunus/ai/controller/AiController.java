package com.yunus.ai.controller;

import com.yunus.ai.dto.AiResultResponse;
import com.yunus.ai.service.AiService;
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
    public AiResultResponse generateReplySuggestion(
            @PathVariable UUID conversationId
    ) {
        // Belirli bir conversation için temsilciye cevap önerisi üretir.
        return aiService.generateReplySuggestion(conversationId);
    }

    @PostMapping("/conversations/{conversationId}/ai/summary")
    public AiResultResponse generateSummary(
            @PathVariable UUID conversationId
    ) {
        // Belirli bir conversation için konuşma özeti üretir.
        return aiService.generateSummary(conversationId);
    }

    @PostMapping("/conversations/{conversationId}/ai/classification")
    public AiResultResponse classifyConversation(
            @PathVariable UUID conversationId
    ) {
        // Belirli bir conversation için intent, sentiment, priority gibi analiz sonucu üretir.
        return aiService.classifyConversation(conversationId);
    }

    @PostMapping("/conversations/{conversationId}/ai/tag-suggestion")
    public AiResultResponse suggestTags(
            @PathVariable UUID conversationId
    ) {
        // Belirli bir conversation için AI destekli tag önerileri üretir.
        return aiService.suggestTags(conversationId);
    }

    @GetMapping("/conversations/{conversationId}/ai-results")
    public PageResponse<AiResultResponse> getConversationAiResults(
            @PathVariable UUID conversationId,
            Pageable pageable
    ) {
        // Belirli bir conversation'a ait AI result geçmişini sayfalı şekilde getirir.
        return aiService.getConversationAiResults(conversationId, pageable);
    }

    @GetMapping("/ai-results/{id}")
    public AiResultResponse getAiResultById(
            @PathVariable UUID id
    ) {
        // Tek bir AI result kaydını id üzerinden getirir.
        return aiService.getById(id);
    }
}