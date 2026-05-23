package com.yunus.ai;

import com.yunus.ai.dto.AiContextMessage;
import com.yunus.ai.dto.AiConversationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AiPromptBuilder, Spring bağımlılığı olmayan saf bir @Component olduğundan
 * MockitoExtension gerekmez; doğrudan new ile örneklenir.
 */
class AiPromptBuilderTest {

    private AiPromptBuilder aiPromptBuilder;
    private AiConversationContext contextWithMessages;
    private AiConversationContext contextWithoutMessages;

    @BeforeEach
    void setUp() {
        aiPromptBuilder = new AiPromptBuilder();

        AiContextMessage customerMsg = new AiContextMessage(
                "CUSTOMER", "Siparişim nerede?", LocalDateTime.now().minusMinutes(5)
        );
        AiContextMessage agentMsg = new AiContextMessage(
                "AGENT", "Kargoda, yarın teslim edilecek.", LocalDateTime.now()
        );

        contextWithMessages = new AiConversationContext(
                UUID.randomUUID(),
                "Kargo Takibi",
                "OPEN",
                UUID.randomUUID(),
                "Ahmet Yılmaz",
                "ACTIVE",
                List.of("premium", "kargo"),
                List.of(customerMsg, agentMsg),
                2
        );

        // Mesajsız context (boş liste)
        contextWithoutMessages = new AiConversationContext(
                UUID.randomUUID(), null, "PENDING",
                UUID.randomUUID(), null, "ACTIVE",
                List.of(), List.of(), 0
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildReplySuggestionPrompt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Reply suggestion prompt boş olmamalı")
    void buildReplySuggestionPrompt_ShouldNotBeBlank() {
        // Act
        String prompt = aiPromptBuilder.buildReplySuggestionPrompt(contextWithMessages);

        // Assert
        assertThat(prompt).isNotBlank();
    }

    @Test
    @DisplayName("Reply suggestion prompt 'Turkish' veya 'Turkish' dil kuralı içermeli")
    void buildReplySuggestionPrompt_ShouldContainTurkishLanguageRule() {
        // Act
        String prompt = aiPromptBuilder.buildReplySuggestionPrompt(contextWithMessages);

        // Assert – prompt içinde Türkçe cevap yazılacağına dair kural olmalı
        assertThat(prompt.toLowerCase()).contains("turkish");
    }

    @Test
    @DisplayName("Reply suggestion prompt conversation context bilgisi (başlık vb.) içermeli")
    void buildReplySuggestionPrompt_ShouldContainConversationContextInfo() {
        // Act
        String prompt = aiPromptBuilder.buildReplySuggestionPrompt(contextWithMessages);

        // Assert – context bilgileri prompt'a dahil edilmiş olmalı
        assertThat(prompt).contains("Kargo Takibi");   // conversation title
        assertThat(prompt).contains("Ahmet Yılmaz");   // customer name
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildSummaryPrompt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Summary prompt içinde conversation context bilgisi bulunmalı")
    void buildSummaryPrompt_ShouldContainConversationContextInfo() {
        // Act
        String prompt = aiPromptBuilder.buildSummaryPrompt(contextWithMessages);

        // Assert
        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("Kargo Takibi");
        assertThat(prompt).contains("Ahmet Yılmaz");
    }

    @Test
    @DisplayName("Summary prompt mesajları kronolojik içermeli")
    void buildSummaryPrompt_ShouldContainMessageContent() {
        // Act
        String prompt = aiPromptBuilder.buildSummaryPrompt(contextWithMessages);

        // Assert – mesaj içerikleri prompt'ta yer almalı
        assertThat(prompt).contains("Siparişim nerede?");
        assertThat(prompt).contains("Kargoda, yarın teslim edilecek.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildClassificationPrompt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Classification prompt yalnızca JSON dönülmesini söylemeli")
    void buildClassificationPrompt_ShouldInstructJsonOnlyOutput() {
        // Act
        String prompt = aiPromptBuilder.buildClassificationPrompt(contextWithMessages);

        // Assert
        assertThat(prompt.toLowerCase()).contains("json");
        assertThat(prompt.toLowerCase()).contains("only");
    }

    @Test
    @DisplayName("Classification prompt intent, sentiment, priority, confidence alanlarını içermeli")
    void buildClassificationPrompt_ShouldContainRequiredJsonFields() {
        // Act
        String prompt = aiPromptBuilder.buildClassificationPrompt(contextWithMessages);

        // Assert
        assertThat(prompt).contains("intent");
        assertThat(prompt).contains("sentiment");
        assertThat(prompt).contains("priority");
        assertThat(prompt).contains("confidence");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildTagSuggestionPrompt
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Tag suggestion prompt suggestedTags JSON formatını içermeli")
    void buildTagSuggestionPrompt_ShouldContainSuggestedTagsJsonFormat() {
        // Act
        String prompt = aiPromptBuilder.buildTagSuggestionPrompt(contextWithMessages);

        // Assert
        assertThat(prompt).contains("suggestedTags");
        assertThat(prompt.toLowerCase()).contains("json");
    }

    @Test
    @DisplayName("Tag suggestion prompt mevcut customer tag listesini içermeli")
    void buildTagSuggestionPrompt_ShouldContainExistingCustomerTags() {
        // Act
        String prompt = aiPromptBuilder.buildTagSuggestionPrompt(contextWithMessages);

        // Assert – customer'ın mevcut tag'leri prompt'ta yer almalı
        assertThat(prompt).contains("premium");
        assertThat(prompt).contains("kargo");
    }

    @Test
    @DisplayName("Tag suggestion prompt context mesajlarını içermeli")
    void buildTagSuggestionPrompt_ShouldContainConversationMessages() {
        // Act
        String prompt = aiPromptBuilder.buildTagSuggestionPrompt(contextWithMessages);

        // Assert
        assertThat(prompt).contains("Siparişim nerede?");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mesaj yoksa – "No messages found" placeholder
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Context'te mesaj yoksa prompt 'No messages found' içermeli")
    void buildReplySuggestionPrompt_WhenNoMessages_ShouldContainNoMessagesPlaceholder() {
        // Act
        String prompt = aiPromptBuilder.buildReplySuggestionPrompt(contextWithoutMessages);

        // Assert
        assertThat(prompt).contains("No messages found");
    }
}
