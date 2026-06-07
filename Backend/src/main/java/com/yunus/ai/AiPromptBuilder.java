package com.yunus.ai;

import com.yunus.ai.dto.AiContextMessage;
import com.yunus.ai.dto.AiConversationContext;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class AiPromptBuilder {

    public String buildReplySuggestionPrompt(AiConversationContext context) {
        // Temsilciye müşteri mesajına karşılık kullanabileceği cevap önerisi üretmek için prompt oluşturur.
        return """
                You are an AI assistant inside a CRM platform.
                Your task is to suggest a helpful reply for the support agent.
                
                Rules:
                - Write the reply in Turkish.
                - Be professional, clear and friendly.
                - Do not invent information that is not present in the conversation.
                - Do not promise refunds, discounts, shipment dates or technical solutions unless clearly mentioned.
                - The reply must be ready for an agent to review, but it must not claim to be automatically sent.
                - Keep the answer concise.
                
                Customer and conversation context:
                %s
                
                Conversation messages:
                %s
                
                Now generate only the suggested reply text.
                """.formatted(
                buildContextInfo(context),
                buildMessagesText(context)
        );
    }

    public String buildSummaryPrompt(AiConversationContext context) {
        // Uzun konuşmaları kısa ve anlaşılır şekilde özetlemek için prompt oluşturur.
        return """
                You are an AI assistant inside a CRM platform.
                Your task is to summarize the conversation for a support agent.
                
                Rules:
                - Write the summary in Turkish.
                - Be concise and clear.
                - Mention the customer's main issue.
                - Mention what has already been answered or offered by the agent if available.
                - Mention the current expected next step if it is clear.
                - Do not invent missing details.
                
                Customer and conversation context:
                %s
                
                Conversation messages:
                %s
                
                Now generate only the conversation summary.
                """.formatted(
                buildContextInfo(context),
                buildMessagesText(context)
        );
    }

    public String buildClassificationPrompt(AiConversationContext context) {
        // Konuşmanın niyet, duygu ve öncelik bilgisini JSON olarak analiz etmek için prompt oluşturur.
        return """
                You are an AI assistant inside a CRM platform.
                Your task is to classify the conversation.
                
                Rules:
                - Return only valid JSON.
                - Do not write explanation outside JSON.
                - Use Turkish understanding, but JSON values must be uppercase English enum-like strings.
                - If you are unsure, use UNKNOWN.
                - confidence must be between 0.0 and 1.0.
                
                JSON format:
                {
                  "intent": "SUPPORT_REQUEST | SALES_LEAD | COMPLAINT | INFORMATION_REQUEST | OTHER | UNKNOWN",
                  "sentiment": "POSITIVE | NEUTRAL | NEGATIVE | ANGRY | UNKNOWN",
                  "priority": "LOW | MEDIUM | HIGH | URGENT | UNKNOWN",
                  "confidence": 0.0
                }
                
                Customer and conversation context:
                %s
                
                Conversation messages:
                %s
                
                Now return only the JSON result.
                """.formatted(
                buildContextInfo(context),
                buildMessagesText(context)
        );
    }

    public String buildTagSuggestionPrompt(AiConversationContext context) {
        // Konuşmaya veya müşteriye uygun olabilecek tag önerilerini JSON olarak üretmek için prompt oluşturur.
        return """
                You are an AI assistant inside a CRM platform.
                Your task is to suggest useful CRM tags for this conversation.
                
                Rules:
                - Return only valid JSON.
                - Do not write explanation outside JSON.
                - Suggested tags must be short.
                - Use lowercase kebab-case format.
                - Suggest maximum 5 tags.
                - Do not duplicate existing customer tags.
                - Do not automatically apply tags; only suggest them.
                
                JSON format:
                {
                  "suggestedTags": ["tag-one", "tag-two"]
                }
                
                Customer and conversation context:
                %s
                
                Existing customer tags:
                %s
                
                Conversation messages:
                %s
                
                Now return only the JSON result.
                """.formatted(
                buildContextInfo(context),
                context.customerTags(),
                buildMessagesText(context)
        );
    }

    private String buildContextInfo(AiConversationContext context) {
        // Conversation ve customer bilgilerini prompt içinde okunabilir hale getirir.
        return """
                Conversation ID: %s
                Conversation Title: %s
                Conversation Status: %s
                Customer ID: %s
                Customer Full Name: %s
                Customer Status: %s
                Customer Tags: %s
                Used Message Count: %d
                """.formatted(
                context.conversationId(),
                nullToDash(context.conversationTitle()),
                nullToDash(context.conversationStatus()),
                context.customerId(),
                nullToDash(context.customerFullName()),
                nullToDash(context.customerStatus()),
                context.customerTags(),
                context.usedMessageCount()
        );
    }

    private String buildMessagesText(AiConversationContext context) {
        // AI'a verilecek mesaj geçmişini eski mesajdan yeni mesaja doğru metne çevirir.
        StringJoiner joiner = new StringJoiner("\n");

        for (AiContextMessage message : context.messages()) {
            // Her mesajı ROLE | sentAt | content formatında prompt'a ekliyoruz.
            joiner.add("[%s] %s: %s".formatted(
                    message.sentAt(),
                    message.role(),
                    message.content()
            ));
        }

        // Hiç mesaj yoksa AI'a bunu açıkça söylüyoruz.
        if (joiner.length() == 0) {
            return "No messages found in this conversation.";
        }

        return joiner.toString();
    }

    private String nullToDash(String value) {
        // Null veya boş değerleri prompt içinde daha okunabilir hale getirir.
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }
}
