package com.yunus.ai.service;

import com.yunus.ai.AiPromptBuilder;
import com.yunus.ai.AiResult;
import com.yunus.ai.AiResultMapper;
import com.yunus.ai.ConversationContextBuilder;
import com.yunus.ai.config.AiProperties;
import com.yunus.ai.dto.AiConversationContext;
import com.yunus.ai.dto.AiContextMessage;
import com.yunus.ai.dto.AiResultResponse;
import com.yunus.ai.enums.AiResultStatus;
import com.yunus.ai.enums.AiResultType;
import com.yunus.ai.provider.AiProvider;
import com.yunus.ai.provider.dto.AiProviderRequest;
import com.yunus.ai.provider.dto.AiProviderResponse;
import com.yunus.ai.repository.AiResultRepository;
import com.yunus.auth.entity.User;
import com.yunus.conversation.Conversation;
import com.yunus.conversation.ConversationRepository;
import com.yunus.customer.Customer;
import com.yunus.enums.ConversationStatus;
import com.yunus.enums.CustomerStatus;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// Bazı @BeforeEach stub'ları hata senaryolarında kullanılmaz; LENIENT ile bu esnekliği sağlıyoruz
@MockitoSettings(strictness = Strictness.LENIENT)
class AiServiceTest {

    @Mock
    private AiResultRepository aiResultRepository;
    @Mock
    private AiResultMapper aiResultMapper;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationContextBuilder conversationContextBuilder;
    @Mock
    private AiPromptBuilder aiPromptBuilder;
    @Mock
    private AiProvider aiProvider;
    @Mock
    private AiProperties aiProperties;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AiService aiService;

    // ── Test verisi ──────────────────────────────────────────────────────────
    private UUID conversationId;
    private Conversation conversation;
    private Customer customer;
    private User requestedBy;
    private AiConversationContext context;
    private AiProviderResponse providerResponse;
    private AiResult savedAiResult;
    private AiResultResponse aiResultResponse;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();

        customer = new Customer();
        customer.setStatus(CustomerStatus.ACTIVE);

        conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(ConversationStatus.OPEN);

        requestedBy = new User();

        // En az 1 mesaj içeren context (validation geçebilsin diye)
        AiContextMessage msg = new AiContextMessage("CUSTOMER", "Merhaba", LocalDateTime.now());
        context = new AiConversationContext(
                conversationId, "Test Conversation", "OPEN",
                UUID.randomUUID(), "Ali Veli", "ACTIVE",
                List.of("vip"), List.of(msg), 1
        );

        providerResponse = new AiProviderResponse("AI yanıtı", 100, 50);

        savedAiResult = new AiResult();
        savedAiResult.setType(AiResultType.REPLY_SUGGESTION);
        savedAiResult.setStatus(AiResultStatus.SUCCESS);
        savedAiResult.setContent("AI yanıtı");

        aiResultResponse = new AiResultResponse(
                UUID.randomUUID(), conversationId, null, null, null,
                AiResultType.REPLY_SUGGESTION, AiResultStatus.SUCCESS,
                "gpt-4o", "v1", "{}", "AI yanıtı", null,
                100, 50, 500, LocalDateTime.now(), LocalDateTime.now()
        );

        // Ortak stub'lar
        when(conversationRepository.findByIdAndIsDeletedFalse(conversationId))
                .thenReturn(Optional.of(conversation));
        when(currentUserService.getCurrentUser()).thenReturn(requestedBy);
        when(conversationContextBuilder.build(conversationId)).thenReturn(context);
        when(aiProperties.getModel()).thenReturn("gpt-4o");
        when(aiProperties.getMaxOutputTokens()).thenReturn(500);
        when(aiProperties.getMaxContextMessages()).thenReturn(20);
        when(aiProperties.getPromptVersion()).thenReturn("v1");
        when(aiResultRepository.save(any(AiResult.class))).thenReturn(savedAiResult);
        when(aiResultMapper.toResponse(savedAiResult)).thenReturn(aiResultResponse);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // generateReplySuggestion – başarılı senaryo
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateReplySuggestion başarılı olduğunda AiResult SUCCESS ve REPLY_SUGGESTION tipinde dönmeli")
    void generateReplySuggestion_WhenSuccess_ShouldReturnSuccessResultWithReplySuggestionType() {
        // Arrange
        when(aiPromptBuilder.buildReplySuggestionPrompt(context)).thenReturn("reply prompt");
        when(aiProvider.generate(any(AiProviderRequest.class))).thenReturn(providerResponse);

        // Act
        AiResultResponse response = aiService.generateReplySuggestion(conversationId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(AiResultStatus.SUCCESS);
        assertThat(response.type()).isEqualTo(AiResultType.REPLY_SUGGESTION);

        // Provider ve repository'nin çağrıldığını doğrula
        verify(aiProvider).generate(any(AiProviderRequest.class));
        verify(aiResultRepository).save(any(AiResult.class));
    }

    @Test
    @DisplayName("generateReplySuggestion provider çağrısında doğru type ile AiResult kaydedilmeli")
    void generateReplySuggestion_ShouldSaveAiResultWithCorrectType() {
        // Arrange
        when(aiPromptBuilder.buildReplySuggestionPrompt(context)).thenReturn("reply prompt");
        when(aiProvider.generate(any(AiProviderRequest.class))).thenReturn(providerResponse);

        // Act
        aiService.generateReplySuggestion(conversationId);

        // Assert – kaydedilen AiResult'ın type alanını kontrol et
        ArgumentCaptor<AiResult> captor = ArgumentCaptor.forClass(AiResult.class);
        verify(aiResultRepository).save(captor.capture());
        AiResult saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(AiResultType.REPLY_SUGGESTION);
        assertThat(saved.getStatus()).isEqualTo(AiResultStatus.SUCCESS);
        assertThat(saved.getContent()).isEqualTo(providerResponse.content());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // generateSummary – başarılı senaryo
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateSummary başarılı olduğunda AiResult CONVERSATION_SUMMARY tipinde ve SUCCESS statüsünde olmalı")
    void generateSummary_WhenSuccess_ShouldReturnSummaryTypeAndSuccessStatus() {
        // Arrange
        AiResult summarySavedResult = new AiResult();
        summarySavedResult.setType(AiResultType.CONVERSATION_SUMMARY);
        summarySavedResult.setStatus(AiResultStatus.SUCCESS);
        summarySavedResult.setContent("Özet içeriği");

        AiResultResponse summaryResponse = new AiResultResponse(
                UUID.randomUUID(), conversationId, null, null, null,
                AiResultType.CONVERSATION_SUMMARY, AiResultStatus.SUCCESS,
                "gpt-4o", "v1", "{}", "Özet içeriği", null,
                100, 50, 500, LocalDateTime.now(), LocalDateTime.now()
        );

        when(aiPromptBuilder.buildSummaryPrompt(context)).thenReturn("summary prompt");
        when(aiProvider.generate(any(AiProviderRequest.class))).thenReturn(providerResponse);
        when(aiResultRepository.save(any(AiResult.class))).thenReturn(summarySavedResult);
        when(aiResultMapper.toResponse(summarySavedResult)).thenReturn(summaryResponse);

        // Act
        AiResultResponse response = aiService.generateSummary(conversationId);

        // Assert
        assertThat(response.type()).isEqualTo(AiResultType.CONVERSATION_SUMMARY);
        assertThat(response.status()).isEqualTo(AiResultStatus.SUCCESS);

        // Provider response content'i AiResult content'ine yazılmalı
        ArgumentCaptor<AiResult> captor = ArgumentCaptor.forClass(AiResult.class);
        verify(aiResultRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo(providerResponse.content());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // classifyConversation – başarılı senaryo
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("classifyConversation başarılı olduğunda AiResult CONVERSATION_CLASSIFICATION tipinde olmalı")
    void classifyConversation_WhenSuccess_ShouldSaveClassificationTypeResult() {
        // Arrange
        AiProviderResponse jsonResponse = new AiProviderResponse(
                "{\"intent\":\"SUPPORT_REQUEST\",\"sentiment\":\"NEUTRAL\",\"priority\":\"LOW\",\"confidence\":0.9}",
                80, 40
        );
        AiResult classifySaved = new AiResult();
        classifySaved.setType(AiResultType.CONVERSATION_CLASSIFICATION);
        classifySaved.setStatus(AiResultStatus.SUCCESS);

        AiResultResponse classifyResp = new AiResultResponse(
                UUID.randomUUID(), conversationId, null, null, null,
                AiResultType.CONVERSATION_CLASSIFICATION, AiResultStatus.SUCCESS,
                "gpt-4o", "v1", "{}", "{...}", null,
                80, 40, 500, LocalDateTime.now(), LocalDateTime.now()
        );

        when(aiPromptBuilder.buildClassificationPrompt(context)).thenReturn("classify prompt");
        when(aiProvider.generate(any(AiProviderRequest.class))).thenReturn(jsonResponse);
        when(aiResultRepository.save(any(AiResult.class))).thenReturn(classifySaved);
        when(aiResultMapper.toResponse(classifySaved)).thenReturn(classifyResp);

        // Act
        AiResultResponse response = aiService.classifyConversation(conversationId);

        // Assert
        assertThat(response.type()).isEqualTo(AiResultType.CONVERSATION_CLASSIFICATION);
        verify(aiResultRepository).save(argThat(r ->
                r.getType() == AiResultType.CONVERSATION_CLASSIFICATION
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // suggestTags – başarılı senaryo
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("suggestTags başarılı olduğunda AiResult TAG_SUGGESTION tipinde ve JSON content içermeli")
    void suggestTags_WhenSuccess_ShouldSaveTagSuggestionType() {
        // Arrange
        AiProviderResponse tagResponse = new AiProviderResponse(
                "{\"suggestedTags\":[\"premium\",\"teknik-destek\"]}",
                60, 30
        );
        AiResult tagSaved = new AiResult();
        tagSaved.setType(AiResultType.TAG_SUGGESTION);
        tagSaved.setStatus(AiResultStatus.SUCCESS);

        AiResultResponse tagResp = new AiResultResponse(
                UUID.randomUUID(), conversationId, null, null, null,
                AiResultType.TAG_SUGGESTION, AiResultStatus.SUCCESS,
                "gpt-4o", "v1", "{}", "{\"suggestedTags\":[]}", null,
                60, 30, 500, LocalDateTime.now(), LocalDateTime.now()
        );

        when(aiPromptBuilder.buildTagSuggestionPrompt(context)).thenReturn("tag prompt");
        when(aiProvider.generate(any(AiProviderRequest.class))).thenReturn(tagResponse);
        when(aiResultRepository.save(any(AiResult.class))).thenReturn(tagSaved);
        when(aiResultMapper.toResponse(tagSaved)).thenReturn(tagResp);

        // Act
        AiResultResponse response = aiService.suggestTags(conversationId);

        // Assert
        assertThat(response.type()).isEqualTo(AiResultType.TAG_SUGGESTION);
        ArgumentCaptor<AiResult> captor = ArgumentCaptor.forClass(AiResult.class);
        verify(aiResultRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo(tagResponse.content());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AI provider BusinessException fırlatırsa – FAILED kaydı
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AI provider BusinessException fırlatırsa AiResult FAILED olarak kaydedilmeli")
    void generateReplySuggestion_WhenProviderThrowsBusinessException_ShouldSaveFailedResult() {
        // Arrange
        when(aiPromptBuilder.buildReplySuggestionPrompt(context)).thenReturn("reply prompt");
        when(aiProvider.generate(any(AiProviderRequest.class)))
                .thenThrow(new BusinessException(ErrorType.AI_PROVIDER_ERROR, "Service unavailable"));

        AiResult failedResult = new AiResult();
        failedResult.setType(AiResultType.REPLY_SUGGESTION);
        failedResult.setStatus(AiResultStatus.FAILED);
        failedResult.setErrorMessage("AI Servisi Şuan Kullanılamıyor Service unavailable");

        AiResultResponse failedResponse = new AiResultResponse(
                UUID.randomUUID(), conversationId, null, null, null,
                AiResultType.REPLY_SUGGESTION, AiResultStatus.FAILED,
                "gpt-4o", "v1", "{}", null, "hata",
                null, null, 500, LocalDateTime.now(), LocalDateTime.now()
        );

        when(aiResultRepository.save(any(AiResult.class))).thenReturn(failedResult);
        when(aiResultMapper.toResponse(failedResult)).thenReturn(failedResponse);

        // Act
        AiResultResponse response = aiService.generateReplySuggestion(conversationId);

        // Assert – FAILED kayıt dönmeli
        assertThat(response.status()).isEqualTo(AiResultStatus.FAILED);

        // FAILED AiResult kaydedildi mi?
        ArgumentCaptor<AiResult> captor = ArgumentCaptor.forClass(AiResult.class);
        verify(aiResultRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AiResultStatus.FAILED);
        assertThat(captor.getValue().getErrorMessage()).isNotBlank();
        assertThat(captor.getValue().getContent()).isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Conversation bulunamazsa – BusinessException
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Conversation bulunamazsa BusinessException fırlatılmalı ve provider / save çağrılmamalı")
    void generateReplySuggestion_WhenConversationNotFound_ShouldThrowBusinessException() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(conversationRepository.findByIdAndIsDeletedFalse(unknownId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> aiService.generateReplySuggestion(unknownId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        // Bu iki bağımlılık kesinlikle çağrılmamalı
        verifyNoInteractions(aiProvider);
        verifyNoInteractions(aiResultRepository);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Context içinde mesaj yoksa – VALIDATION_ERROR
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Context'te hiç mesaj yoksa VALIDATION_ERROR fırlatılmalı ve provider çağrılmamalı")
    void generateReplySuggestion_WhenContextHasNoMessages_ShouldThrowValidationError() {
        // Arrange – usedMessageCount = 0 olan boş context
        AiConversationContext emptyContext = new AiConversationContext(
                conversationId, "Test", "OPEN",
                UUID.randomUUID(), "Ali", "ACTIVE",
                List.of(), List.of(), 0
        );
        when(conversationContextBuilder.build(conversationId)).thenReturn(emptyContext);

        // Act & Assert
        assertThatThrownBy(() -> aiService.generateReplySuggestion(conversationId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.VALIDATION_ERROR));

        // Provider ve repository çağrılmamalı
        verifyNoInteractions(aiProvider);
        verifyNoInteractions(aiResultRepository);
    }

    @Test
    @DisplayName("Başarılı generateReplySuggestion'da doğru conversation ve user bilgileri AiResult'a atanmalı")
    void generateReplySuggestion_WhenSuccess_ShouldSetConversationAndRequestedBy() {
        // Arrange
        when(aiPromptBuilder.buildReplySuggestionPrompt(context)).thenReturn("prompt");
        when(aiProvider.generate(any())).thenReturn(providerResponse);

        // Act
        aiService.generateReplySuggestion(conversationId);

        // Assert
        ArgumentCaptor<AiResult> captor = ArgumentCaptor.forClass(AiResult.class);
        verify(aiResultRepository).save(captor.capture());
        AiResult saved = captor.getValue();
        assertThat(saved.getConversation()).isSameAs(conversation);
        assertThat(saved.getRequestedBy()).isSameAs(requestedBy);
        assertThat(saved.getCustomer()).isSameAs(customer);
    }
}
