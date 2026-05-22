package com.yunus.ai.service;

import com.yunus.ai.*;
import com.yunus.ai.config.AiProperties;
import com.yunus.ai.dto.AiConversationContext;
import com.yunus.ai.dto.AiResultResponse;
import com.yunus.ai.enums.AiResultStatus;
import com.yunus.ai.enums.AiResultType;
import com.yunus.ai.provider.AiProvider;
import com.yunus.ai.provider.dto.AiProviderRequest;
import com.yunus.ai.provider.dto.AiProviderResponse;
import com.yunus.ai.repository.AiResultRepository;
import com.yunus.auth.entity.User;
import com.yunus.common.PageResponse;
import com.yunus.conversation.Conversation;
import com.yunus.conversation.ConversationRepository;
import com.yunus.customer.Customer;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiResultRepository aiResultRepository;
    private final AiResultMapper aiResultMapper;
    private final ConversationRepository conversationRepository;
    private final ConversationContextBuilder conversationContextBuilder;
    private final AiPromptBuilder aiPromptBuilder;
    private final AiProvider aiProvider;
    private final AiProperties aiProperties;
    private final CurrentUserService currentUserService;

    @Transactional
    public AiResultResponse generateReplySuggestion(UUID conversationId) {
        // Temsilciye müşteri mesajına karşılık kullanabileceği cevap önerisi üretir.
        return generateConversationAiResult(
                conversationId,
                AiResultType.REPLY_SUGGESTION
        );
    }

    @Transactional
    public AiResultResponse generateSummary(UUID conversationId) {
        // Konuşma geçmişini temsilci için kısa ve anlaşılır şekilde özetler.
        return generateConversationAiResult(
                conversationId,
                AiResultType.CONVERSATION_SUMMARY
        );
    }

    @Transactional
    public AiResultResponse classifyConversation(UUID conversationId) {
        // Konuşmanın intent, sentiment, priority gibi analiz sonucunu JSON olarak üretir.
        return generateConversationAiResult(
                conversationId,
                AiResultType.CONVERSATION_CLASSIFICATION
        );
    }

    @Transactional
    public AiResultResponse suggestTags(UUID conversationId) {
        // Konuşmaya veya müşteriye uygun olabilecek tag önerilerini JSON olarak üretir.
        return generateConversationAiResult(
                conversationId,
                AiResultType.TAG_SUGGESTION
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AiResultResponse> getConversationAiResults(UUID conversationId, Pageable pageable) {
        // Önce conversation gerçekten var mı ve silinmemiş mi kontrol ediyoruz.
        findActiveConversationById(conversationId);

        // Conversation'a ait AI sonuçlarını yeniden eskiye doğru getiriyoruz.
        Page<AiResultResponse> responsePage = aiResultRepository
                .findAllByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(aiResultMapper::toResponse);

        // Projedeki standart pagination wrapper'ı ile response dönüyoruz.
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public AiResultResponse getById(UUID id) {
        // Tek bir AI result kaydını id üzerinden getiriyoruz.
        AiResult aiResult = aiResultRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "AI result not found"));

        // Entity'yi dışarı doğrudan açmadan response DTO'ya çeviriyoruz.
        return aiResultMapper.toResponse(aiResult);
    }

    private AiResultResponse generateConversationAiResult(UUID conversationId, AiResultType type) {
        // AI sonucu mutlaka bir conversation'a bağlı olacağı için önce conversation'ı buluyoruz.
        Conversation conversation = findActiveConversationById(conversationId);

        // Conversation'a bağlı customer bilgisini alıyoruz.
        // Customer nullable değil varsayıyoruz çünkü conversation customer'a bağlı oluşturuluyor.
        Customer customer = conversation.getCustomer();

        // AI isteğini yapan login olmuş kullanıcıyı alıyoruz.
        User requestedBy = currentUserService.getCurrentUser();

        // Conversation, customer, tag ve son mesajlardan AI context oluşturuyoruz.
        AiConversationContext context = conversationContextBuilder.build(conversationId);

        // Context içinde hiç mesaj yoksa AI çağrısı yapmanın anlamı yok.
        if (context.usedMessageCount() == 0) {
            throw new BusinessException(
                    ErrorType.VALIDATION_ERROR,
                    "Conversation has no messages for AI processing"
            );
        }

        // AI result türüne göre uygun prompt metnini oluşturuyoruz.
        String prompt = buildPromptByType(type, context);

        // Provider'a gönderilecek sade request modelini oluşturuyoruz.
        AiProviderRequest providerRequest = new AiProviderRequest(
                prompt,
                aiProperties.getModel(),
                aiProperties.getMaxOutputTokens()
        );

        try {
            // OpenAI provider'a prompt gönderiliyor ve AI cevabı alınıyor.
            AiProviderResponse providerResponse = aiProvider.generate(providerRequest);

            // Başarılı AI cevabı AiResult entity olarak hazırlanıyor.
            AiResult successResult = buildSuccessResult(
                    conversation,
                    customer,
                    requestedBy,
                    type,
                    context,
                    providerResponse
            );

            // Başarılı sonuç database'e kaydediliyor.
            AiResult savedResult = aiResultRepository.save(successResult);

            // Kaydedilen entity response DTO'ya çevrilip controller'a dönüyor.
            return aiResultMapper.toResponse(savedResult);

        } catch (BusinessException ex) {
            // AI provider tarafında beklenen bir hata oluşursa FAILED kayıt oluşturuyoruz.
            AiResult failedResult = buildFailedResult(
                    conversation,
                    customer,
                    requestedBy,
                    type,
                    context,
                    ex.getMessage()
            );

            // Hatalı AI çağrısını da DB'ye kaydediyoruz.
            AiResult savedFailedResult = aiResultRepository.save(failedResult);

            // API'ye FAILED status'lü AI result dönüyoruz.
            // Böylece temsilci AI çağrısının başarısız olduğunu geçmişte görebilir.
            return aiResultMapper.toResponse(savedFailedResult);
        }
    }

    private String buildPromptByType(AiResultType type, AiConversationContext context) {
        // AI result type'a göre ilgili prompt builder metodunu seçiyoruz.
        return switch (type) {
            case REPLY_SUGGESTION -> aiPromptBuilder.buildReplySuggestionPrompt(context);
            case CONVERSATION_SUMMARY -> aiPromptBuilder.buildSummaryPrompt(context);
            case CONVERSATION_CLASSIFICATION -> aiPromptBuilder.buildClassificationPrompt(context);
            case TAG_SUGGESTION -> aiPromptBuilder.buildTagSuggestionPrompt(context);
        };
    }

    private AiResult buildSuccessResult(
            Conversation conversation,
            Customer customer,
            User requestedBy,
            AiResultType type,
            AiConversationContext context,
            AiProviderResponse providerResponse
    ) {
        // Başarılı AI çıktısı için yeni AiResult entity oluşturuyoruz.
        AiResult aiResult = new AiResult();

        // AI sonucunun hangi conversation'a ait olduğunu set ediyoruz.
        aiResult.setConversation(conversation);

        // Customer nullable olsa da Faz 3'te conversation üzerinden customer bilgisini kaydediyoruz.
        aiResult.setCustomer(customer);

        // AI isteğini yapan kullanıcıyı kaydediyoruz.
        aiResult.setRequestedBy(requestedBy);

        // AI çıktısının türünü kaydediyoruz.
        aiResult.setType(type);

        // Bu kayıt başarılı AI çağrısı olduğu için SUCCESS set ediyoruz.
        aiResult.setStatus(AiResultStatus.SUCCESS);

        // Kullanılan model bilgisini config üzerinden kaydediyoruz.
        aiResult.setModel(aiProperties.getModel());

        // Prompt versiyonunu kaydediyoruz.
        aiResult.setPromptVersion(aiProperties.getPromptVersion());

        // AI context hakkında kısa metadata bilgisini kaydediyoruz.
        aiResult.setInputSnapshot(buildInputSnapshot(context));

        // AI'ın ürettiği asıl cevabı content alanına kaydediyoruz.
        aiResult.setContent(providerResponse.content());

        // Başarılı çağrıda hata mesajı olmadığı için null bırakıyoruz.
        aiResult.setErrorMessage(null);

        // Input token bilgisini kaydediyoruz.
        aiResult.setInputTokenEstimate(providerResponse.inputTokenEstimate());

        // Output token bilgisini kaydediyoruz.
        aiResult.setOutputTokenEstimate(providerResponse.outputTokenEstimate());

        // Bu çağrıda kullanılan maksimum output token limitini kaydediyoruz.
        aiResult.setMaxOutputTokens(aiProperties.getMaxOutputTokens());

        return aiResult;
    }

    private AiResult buildFailedResult(
            Conversation conversation,
            Customer customer,
            User requestedBy,
            AiResultType type,
            AiConversationContext context,
            String errorMessage
    ) {
        // Başarısız AI çağrısı için yeni AiResult entity oluşturuyoruz.
        AiResult aiResult = new AiResult();

        // Başarısız olsa bile hangi conversation için denendiğini kaydediyoruz.
        aiResult.setConversation(conversation);

        // Customer bilgisini conversation üzerinden kaydediyoruz.
        aiResult.setCustomer(customer);

        // AI çağrısını yapan kullanıcıyı kaydediyoruz.
        aiResult.setRequestedBy(requestedBy);

        // Hangi AI işlemi başarısız oldu bilgisini kaydediyoruz.
        aiResult.setType(type);

        // Bu kayıt başarısız AI çağrısı olduğu için FAILED set ediyoruz.
        aiResult.setStatus(AiResultStatus.FAILED);

        // Kullanılan model bilgisini kaydediyoruz.
        aiResult.setModel(aiProperties.getModel());

        // Prompt versiyon bilgisini kaydediyoruz.
        aiResult.setPromptVersion(aiProperties.getPromptVersion());

        // Hangi context ile denendiğini özet olarak kaydediyoruz.
        aiResult.setInputSnapshot(buildInputSnapshot(context));

        // Başarısız çağrıda AI çıktısı olmadığı için content null kalır.
        aiResult.setContent(null);

        // Hata mesajını kaydediyoruz.
        aiResult.setErrorMessage(errorMessage);

        // Başarısız durumda input token tahminini context üzerinden hesaplamıyoruz, null bırakıyoruz.
        aiResult.setInputTokenEstimate(null);

        // Başarısız durumda output token oluşmadığı için null bırakıyoruz.
        aiResult.setOutputTokenEstimate(null);

        // Bu çağrıda kullanılan maksimum output token limitini yine kaydediyoruz.
        aiResult.setMaxOutputTokens(aiProperties.getMaxOutputTokens());

        return aiResult;
    }

    private String buildInputSnapshot(AiConversationContext context) {
        // Faz 3 kararımız gereği inputSnapshot içine tüm mesajları değil,
        // sadece kaç mesaj kullanıldığını ve max context limitini yazıyoruz.
        return """
                {"lastMessageCount":%d,"maxContextMessages":%d}
                """.formatted(
                context.usedMessageCount(),
                aiProperties.getMaxContextMessages()
        ).trim();
    }

    private Conversation findActiveConversationById(UUID id) {
        // Soft delete edilmemiş conversation kaydını bulur.
        return conversationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Conversation not found"));
    }
}