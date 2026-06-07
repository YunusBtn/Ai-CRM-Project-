package com.yunus.ai.dto;

import java.util.List;
import java.util.UUID;

public record AiConversationContext(

        // AI işlemi yapılacak conversation id bilgisidir.
        UUID conversationId,

        // Conversation başlığıdır. Null olabilir.
        String conversationTitle,

        // Conversation status bilgisidir.
        // Örnek: OPEN, PENDING, CLOSED
        String conversationStatus,

        // Conversation'a bağlı customer id bilgisidir.
        UUID customerId,

        // Customer ad soyad bilgisidir.
        // Prompt içinde müşteriyi daha doğal tanıtmak için kullanılır.
        String customerFullName,

        // Customer status bilgisidir.
        // Örnek: ACTIVE, PASSIVE, BLOCKED
        String customerStatus,

        // Customer'a bağlı tag isimleridir.
        // AI tag önerisi veya öncelik yorumu yaparken mevcut tag'leri görebilir.
        List<String> customerTags,

        // AI context'e dahil edilen son mesajlardır.
        // Bu liste kronolojik sırada olmalı: eski mesaj → yeni mesaj.
        List<AiContextMessage> messages,

        // Context oluşturulurken kaç mesaj kullanıldığını belirtir.
        // inputSnapshot alanında da bunu saklayacağız.
        int usedMessageCount

) {
}
