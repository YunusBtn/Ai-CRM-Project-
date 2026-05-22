package com.yunus.ai;

import com.yunus.ai.config.AiProperties;
import com.yunus.ai.dto.AiContextMessage;
import com.yunus.ai.dto.AiConversationContext;
import com.yunus.conversation.Conversation;
import com.yunus.conversation.ConversationRepository;
import com.yunus.customer.Customer;
import com.yunus.enums.SenderType;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.message.Message;
import com.yunus.message.MessageRepository;
import com.yunus.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service // Bu sınıfın Spring tarafından service bean'i olarak yönetilmesini sağlar.
@RequiredArgsConstructor // final alanlar için constructor üretir.
public class ConversationContextBuilder {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AiProperties aiProperties;

    public AiConversationContext build(UUID conversationId) {
        // Önce aktif ve silinmemiş conversation kaydını buluyoruz.
        Conversation conversation = conversationRepository.findByIdAndIsDeletedFalse(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Conversation not found"));

        // Conversation'a bağlı customer bilgisini alıyoruz.
        Customer customer = conversation.getCustomer();

        // AI context için kaç mesaj kullanılacağını config'ten okuyoruz.
        int maxContextMessages = aiProperties.getMaxContextMessages();

        // Son N mesajı veritabanından yeniden eskiye doğru çekiyoruz.
        List<Message> latestMessages = messageRepository.findByConversationIdOrderBySentAtDesc(
                conversationId,
                PageRequest.of(0, maxContextMessages)
        );

        // AI'ın konuşmayı doğru anlaması için mesajları kronolojik sıraya çeviriyoruz.
        // Repository'den yeni → eski geldi, burada eski → yeni yapıyoruz.
        Collections.reverse(latestMessages);

        // Message entity listesini AI için daha sade olan AiContextMessage listesine çeviriyoruz.
        List<AiContextMessage> contextMessages = latestMessages.stream()
                .map(this::toContextMessage)
                .toList();

        // Customer tag entity'lerini sadece tag name listesine dönüştürüyoruz.
        List<String> customerTags = customer.getTags()
                .stream()
                .filter(tag -> !tag.isDeleted()) // Soft delete edilmiş tag'leri AI context'e dahil etmiyoruz.
                .map(Tag::getName) // AI için sadece tag adı yeterli.
                .toList();

        // Tüm sadeleştirilmiş conversation context bilgisini tek DTO'da topluyoruz.
        return new AiConversationContext(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getStatus().name(),
                customer.getId(),
                buildCustomerFullName(customer),
                customer.getStatus().name(),
                customerTags,
                contextMessages,
                contextMessages.size()
        );
    }

    private AiContextMessage toContextMessage(Message message) {
        // Message içindeki SenderType bilgisini AI'ın anlayacağı role metnine çeviriyoruz.
        String role = mapSenderTypeToRole(message.getSenderType());

        // AI context mesajını sade bir record olarak oluşturuyoruz.
        return new AiContextMessage(
          role,message.getContent(),message.getSentAt());
    }

    private String mapSenderTypeToRole(SenderType senderType) {
        // Müşteriden gelen mesajlar AI'a CUSTOMER olarak verilir.
        if (senderType == SenderType.CUSTOMER) {
            return "CUSTOMER";
        }

        // Temsilci tarafından yazılan mesajlar AI'a AGENT olarak verilir.
        if (senderType == SenderType.AGENT) {
            return "AGENT";
        }

        // Sistem mesajları AI'a SYSTEM olarak verilir.
        if (senderType == SenderType.SYSTEM) {
            return "SYSTEM";
        }

        // Beklenmeyen sender type olursa güvenli default değer döneriz.
        return "UNKNOWN";
    }

    private String buildCustomerFullName(Customer customer) {
        // Customer firstName bilgisini null güvenli şekilde alıyoruz.
        String firstName = customer.getFirstName() != null
                ? customer.getFirstName()
                : "";

        // Customer lastName bilgisini null güvenli şekilde alıyoruz.
        String lastName = customer.getLastName() != null
                ? customer.getLastName()
                : "";

        // Ad ve soyadı birleştirip gereksiz boşlukları temizliyoruz.
        String fullName = (firstName + " " + lastName).trim();

        // Eğer ad soyad tamamen boşsa null dönüyoruz.
        return fullName.isEmpty() ? null : fullName;
    }
}