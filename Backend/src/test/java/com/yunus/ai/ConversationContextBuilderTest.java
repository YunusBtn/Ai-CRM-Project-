package com.yunus.ai;

import com.yunus.ai.config.AiProperties;
import com.yunus.ai.dto.AiConversationContext;
import com.yunus.conversation.Conversation;
import com.yunus.conversation.ConversationRepository;
import com.yunus.customer.Customer;
import com.yunus.enums.ConversationStatus;
import com.yunus.enums.CustomerStatus;
import com.yunus.enums.MessageDirection;
import com.yunus.enums.SenderType;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.message.Message;
import com.yunus.message.MessageRepository;
import com.yunus.tag.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// @BeforeEach stub'larının bazı testlerde kullanılmamasını tolere etmek için LENIENT
@MockitoSettings(strictness = Strictness.LENIENT)
class ConversationContextBuilderTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private AiProperties aiProperties;

    @InjectMocks
    private ConversationContextBuilder conversationContextBuilder;

    private UUID conversationId;
    private Customer customer;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();

        customer = new Customer();
        customer.setFirstName("Ali");
        customer.setLastName("Veli");
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setTags(new HashSet<>());

        conversation = new Conversation();
        conversation.setTitle("Sipariş Sorunu");
        conversation.setStatus(ConversationStatus.OPEN);
        conversation.setCustomer(customer);

        // Varsayılan config: son 20 mesaj
        when(aiProperties.getMaxContextMessages()).thenReturn(20);
        when(conversationRepository.findByIdAndIsDeletedFalse(conversationId))
                .thenReturn(Optional.of(conversation));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Başarılı context build – conversation ve customer bilgileri
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Aktif conversation bulunduğunda conversation bilgileri context'e doğru taşınmalı")
    void build_WhenConversationExists_ShouldMapConversationInfo() {
        // Arrange
        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(conversationId), any(PageRequest.class)))
                .thenReturn(List.of());

        // Act
        AiConversationContext ctx = conversationContextBuilder.build(conversationId);

        // Assert
        assertThat(ctx.conversationTitle()).isEqualTo("Sipariş Sorunu");
        assertThat(ctx.conversationStatus()).isEqualTo("OPEN");
        // Not: ctx.conversationId() == conversation.getId() — BaseEntity'de @PrePersist
        // çalışmadığından id null olur, bu yüzden sadece title ve status'u doğruluyoruz
        assertThat(ctx.conversationTitle()).isNotNull();
    }

    @Test
    @DisplayName("Aktif conversation bulunduğunda customer bilgileri context'e doğru taşınmalı")
    void build_WhenConversationExists_ShouldMapCustomerInfo() {
        // Arrange
        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(conversationId), any(PageRequest.class)))
                .thenReturn(List.of());

        // Act
        AiConversationContext ctx = conversationContextBuilder.build(conversationId);

        // Assert
        assertThat(ctx.customerFullName()).isEqualTo("Ali Veli");
        assertThat(ctx.customerStatus()).isEqualTo("ACTIVE");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tag filtreleme – soft delete edilmiş tag dahil edilmemeli
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Soft delete edilmemiş tag'ler context'e eklenmeli")
    void build_WhenCustomerHasActiveTags_ShouldIncludeTagNames() {
        // Arrange
        Tag activeTag = new Tag();
        activeTag.setName("vip");
        activeTag.setDeleted(false);

        Set<Tag> tags = new HashSet<>();
        tags.add(activeTag);
        customer.setTags(tags);

        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(conversationId), any(PageRequest.class)))
                .thenReturn(List.of());

        // Act
        AiConversationContext ctx = conversationContextBuilder.build(conversationId);

        // Assert
        assertThat(ctx.customerTags()).contains("vip");
    }

    @Test
    @DisplayName("Soft delete edilmiş tag'ler context'e dahil edilmemeli")
    void build_WhenCustomerHasDeletedTag_ShouldExcludeDeletedTags() {
        // Arrange
        Tag deletedTag = new Tag();
        deletedTag.setName("eski-tag");
        deletedTag.setDeleted(true);

        Tag activeTag = new Tag();
        activeTag.setName("aktif-tag");
        activeTag.setDeleted(false);

        Set<Tag> tags = new HashSet<>();
        tags.add(deletedTag);
        tags.add(activeTag);
        customer.setTags(tags);

        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(conversationId), any(PageRequest.class)))
                .thenReturn(List.of());

        // Act
        AiConversationContext ctx = conversationContextBuilder.build(conversationId);

        // Assert
        assertThat(ctx.customerTags()).containsOnly("aktif-tag");
        assertThat(ctx.customerTags()).doesNotContain("eski-tag");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mesaj sıralaması: repository'den yeni→eski, context'e eski→yeni
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Repository'den yeni→eski gelen mesajlar context'e eski→yeni sıralanmalı")
    void build_WhenMessagesExist_ShouldReverseMessageOrder() {
        // Arrange
        LocalDateTime earlier = LocalDateTime.now().minusMinutes(5);
        LocalDateTime later = LocalDateTime.now();

        // Repository yeni → eski döner (later önce, earlier sonra)
        // Collections.reverse() için mütable liste döndürmek gerekiyor
        Message newMsg = new Message();
        newMsg.setContent("Yeni mesaj");
        newMsg.setSenderType(SenderType.AGENT);
        newMsg.setSentAt(later);

        Message oldMsg = new Message();
        oldMsg.setContent("Eski mesaj");
        oldMsg.setSenderType(SenderType.CUSTOMER);
        oldMsg.setSentAt(earlier);

        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(conversationId), any(PageRequest.class)))
                .thenReturn(new ArrayList<>(List.of(newMsg, oldMsg))); // mutable liste

        // Act
        AiConversationContext ctx = conversationContextBuilder.build(conversationId);

        // Assert – context'te eski → yeni olmalı
        assertThat(ctx.messages()).hasSize(2);
        assertThat(ctx.messages().get(0).content()).isEqualTo("Eski mesaj"); // eski önde
        assertThat(ctx.messages().get(1).content()).isEqualTo("Yeni mesaj"); // yeni sonda
    }

    @Test
    @DisplayName("Mesaj sayısı usedMessageCount alanına doğru yazılmalı")
    void build_WhenMessagesExist_ShouldSetUsedMessageCount() {
        // Arrange
        Message msg1 = new Message();
        msg1.setContent("Mesaj 1");
        msg1.setSenderType(SenderType.CUSTOMER);
        msg1.setSentAt(LocalDateTime.now().minusMinutes(2));

        Message msg2 = new Message();
        msg2.setContent("Mesaj 2");
        msg2.setSenderType(SenderType.AGENT);
        msg2.setSentAt(LocalDateTime.now().minusMinutes(1));

        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(conversationId), any(PageRequest.class)))
                .thenReturn(new ArrayList<>(List.of(msg2, msg1))); // mutable liste gerekli

        // Act
        AiConversationContext ctx = conversationContextBuilder.build(conversationId);

        // Assert
        assertThat(ctx.usedMessageCount()).isEqualTo(2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Conversation bulunamazsa – NOT_FOUND
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Conversation bulunamazsa NOT_FOUND BusinessException fırlatılmalı")
    void build_WhenConversationNotFound_ShouldThrowNotFoundBusinessException() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(conversationRepository.findByIdAndIsDeletedFalse(unknownId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> conversationContextBuilder.build(unknownId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        // MessageRepository kesinlikle çağrılmamalı
        verifyNoInteractions(messageRepository);
    }

    @Test
    @DisplayName("AGENT senderType'ı context'te AGENT role'üne map edilmeli")
    void build_WhenMessageSenderIsAgent_ShouldMapRoleToAgent() {
        // Arrange
        Message agentMsg = new Message();
        agentMsg.setContent("Agent cevabı");
        agentMsg.setSenderType(SenderType.AGENT);
        agentMsg.setSentAt(LocalDateTime.now());

        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(conversationId), any(PageRequest.class)))
                .thenReturn(List.of(agentMsg));

        // Act
        AiConversationContext ctx = conversationContextBuilder.build(conversationId);

        // Assert
        assertThat(ctx.messages().get(0).role()).isEqualTo("AGENT");
    }

    @Test
    @DisplayName("SYSTEM senderType'ı context'te SYSTEM role'üne map edilmeli")
    void build_WhenMessageSenderIsSystem_ShouldMapRoleToSystem() {
        // Arrange
        Message sysMsg = new Message();
        sysMsg.setContent("Sistem mesajı");
        sysMsg.setSenderType(SenderType.SYSTEM);
        sysMsg.setSentAt(LocalDateTime.now());

        when(messageRepository.findByConversationIdOrderBySentAtDesc(eq(conversationId), any(PageRequest.class)))
                .thenReturn(List.of(sysMsg));

        // Act
        AiConversationContext ctx = conversationContextBuilder.build(conversationId);

        // Assert
        assertThat(ctx.messages().get(0).role()).isEqualTo("SYSTEM");
    }
}
