package com.yunus.message;

import com.yunus.auth.entity.User;
import com.yunus.common.PageResponse;
import com.yunus.conversation.Conversation;
import com.yunus.conversation.ConversationRepository;
import com.yunus.enums.ConversationStatus;
import com.yunus.enums.MessageDirection;
import com.yunus.enums.SenderType;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.message.dto.MessageCreateRequest;
import com.yunus.message.dto.MessageResponse;
import com.yunus.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// @BeforeEach conversation stub bazı testlerde (not found) kullanılmaz; LENIENT ile bu esnekliği sağlıyoruz
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private MessageService messageService;

    private UUID conversationId;
    private Conversation conversation;
    private User agentUser;
    private Message savedMessage;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();

        conversation = new Conversation();
        conversation.setStatus(ConversationStatus.OPEN);

        agentUser = new User();
        agentUser.setEmail("agent@test.com");

        savedMessage = new Message();
        savedMessage.setContent("Test mesajı");
        savedMessage.setDirection(MessageDirection.OUTBOUND);
        savedMessage.setSenderType(SenderType.AGENT);
        savedMessage.setSentAt(LocalDateTime.now());

        messageResponse = new MessageResponse(
                UUID.randomUUID(), conversationId,
                "Test mesajı",
                MessageDirection.OUTBOUND, SenderType.AGENT,
                null, null, LocalDateTime.now(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        // Conversation bulma stub'ı (çoğu test için geçerli)
        when(conversationRepository.findByIdAndIsDeletedFalse(conversationId))
                .thenReturn(Optional.of(conversation));
    }

    @AfterEach
    void tearDown() {
        // Security context'i her testten sonra temizle
        SecurityContextHolder.clearContext();
    }

    // ─── SecurityContext helper ───────────────────────────────────────────────

    /**
     * Test sırasında SecurityContextHolder'a AGENT kullanıcısı set eder.
     */
    private void setAgentInSecurityContext(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AGENT mesajı oluşturma
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AGENT OUTBOUND mesajı oluşturulduğunda current user senderUser olarak atanmalı")
    void create_WhenAgentMessage_ShouldSetCurrentUserAsSenderUser() {
        // Arrange
        setAgentInSecurityContext(agentUser);
        MessageCreateRequest request = new MessageCreateRequest(
                "Merhaba, nasıl yardımcı olabilirim?",
                MessageDirection.OUTBOUND, SenderType.AGENT
        );

        Message mappedMessage = new Message();
        mappedMessage.setContent(request.content());
        mappedMessage.setDirection(MessageDirection.OUTBOUND);
        mappedMessage.setSenderType(SenderType.AGENT);

        when(messageMapper.toEntity(request)).thenReturn(mappedMessage);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageMapper.toResponse(savedMessage)).thenReturn(messageResponse);

        // Act
        MessageResponse response = messageService.create(conversationId, request);

        // Assert
        assertThat(mappedMessage.getSenderUser()).isSameAs(agentUser);
        assertThat(mappedMessage.getSentAt()).isNotNull();
        verify(messageRepository).save(any(Message.class));
        verify(conversationRepository).save(conversation);
    }

    @Test
    @DisplayName("AGENT mesajı oluşturulduğunda conversation.lastMessageAt güncellenmeli")
    void create_WhenAgentMessage_ShouldUpdateConversationLastMessageAt() {
        // Arrange
        setAgentInSecurityContext(agentUser);
        MessageCreateRequest request = new MessageCreateRequest(
                "Mesaj", MessageDirection.OUTBOUND, SenderType.AGENT
        );

        Message mappedMessage = new Message();
        mappedMessage.setSentAt(LocalDateTime.now());

        when(messageMapper.toEntity(request)).thenReturn(mappedMessage);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageMapper.toResponse(savedMessage)).thenReturn(messageResponse);

        // Act
        messageService.create(conversationId, request);

        // Assert – conversation.lastMessageAt set edilmiş olmalı
        assertThat(conversation.getLastMessageAt()).isNotNull();
        verify(conversationRepository).save(conversation);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOMER inbound mesajı
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CUSTOMER INBOUND mesajı oluşturulduğunda senderUser null olmalı")
    void create_WhenCustomerInboundMessage_ShouldHaveNullSenderUser() {
        // Arrange – CUSTOMER mesajında security context gerekmez
        MessageCreateRequest request = new MessageCreateRequest(
                "Siparişim nerede?", MessageDirection.INBOUND, SenderType.CUSTOMER
        );

        Message mappedMessage = new Message();
        mappedMessage.setContent(request.content());
        mappedMessage.setDirection(MessageDirection.INBOUND);
        mappedMessage.setSenderType(SenderType.CUSTOMER);

        when(messageMapper.toEntity(request)).thenReturn(mappedMessage);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageMapper.toResponse(savedMessage)).thenReturn(messageResponse);

        // Act
        messageService.create(conversationId, request);

        // Assert – CUSTOMER mesajında senderUser set edilmemeli
        assertThat(mappedMessage.getSenderUser()).isNull();
        verify(messageRepository).save(any(Message.class));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SYSTEM outbound mesajı
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("SYSTEM OUTBOUND mesajı oluşturulduğunda senderUser null olmalı")
    void create_WhenSystemOutboundMessage_ShouldHaveNullSenderUser() {
        // Arrange
        MessageCreateRequest request = new MessageCreateRequest(
                "Sistem bildirimi", MessageDirection.OUTBOUND, SenderType.SYSTEM
        );

        Message mappedMessage = new Message();
        mappedMessage.setDirection(MessageDirection.OUTBOUND);
        mappedMessage.setSenderType(SenderType.SYSTEM);

        when(messageMapper.toEntity(request)).thenReturn(mappedMessage);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageMapper.toResponse(savedMessage)).thenReturn(messageResponse);

        // Act
        messageService.create(conversationId, request);

        // Assert
        assertThat(mappedMessage.getSenderUser()).isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geçersiz direction + senderType kombinasyonu
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("INBOUND + AGENT kombinasyonu geçersiz olduğunda VALIDATION_ERROR fırlatılmalı")
    void create_WhenInvalidDirectionSenderTypeCombination_ShouldThrowValidationError() {
        // Arrange – INBOUND + AGENT geçersiz kombinasyon
        MessageCreateRequest request = new MessageCreateRequest(
                "Mesaj", MessageDirection.INBOUND, SenderType.AGENT
        );

        // Act & Assert
        assertThatThrownBy(() -> messageService.create(conversationId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.VALIDATION_ERROR));

        // Geçersiz kombinasyonda save çağrılmamalı
        verifyNoInteractions(messageRepository);
    }

    @Test
    @DisplayName("OUTBOUND + CUSTOMER kombinasyonu geçersiz olduğunda VALIDATION_ERROR fırlatılmalı")
    void create_WhenOutboundCustomerCombination_ShouldThrowValidationError() {
        // Arrange
        MessageCreateRequest request = new MessageCreateRequest(
                "Mesaj", MessageDirection.OUTBOUND, SenderType.CUSTOMER
        );

        // Act & Assert
        assertThatThrownBy(() -> messageService.create(conversationId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.VALIDATION_ERROR));

        verifyNoInteractions(messageRepository);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Conversation bulunamazsa
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Conversation bulunamazsa NOT_FOUND BusinessException fırlatılmalı")
    void create_WhenConversationNotFound_ShouldThrowNotFoundBusinessException() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(conversationRepository.findByIdAndIsDeletedFalse(unknownId))
                .thenReturn(Optional.empty());

        MessageCreateRequest request = new MessageCreateRequest(
                "Mesaj", MessageDirection.INBOUND, SenderType.CUSTOMER
        );

        // Act & Assert
        assertThatThrownBy(() -> messageService.create(unknownId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verifyNoInteractions(messageRepository);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getByConversationId – search parametresi boş
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByConversationId search parametresi boşsa findAllByConversationId çağrılmalı")
    void getByConversationId_WhenSearchIsBlank_ShouldCallFindAllByConversationId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messagePage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(messageRepository.findAllByConversationId(conversationId, pageable))
                .thenReturn(messagePage);

        // Act
        PageResponse<MessageResponse> result = messageService.getByConversationId(conversationId, null, pageable);

        // Assert
        assertThat(result).isNotNull();
        verify(messageRepository).findAllByConversationId(conversationId, pageable);
        verify(messageRepository, never())
                .findAllByConversationIdAndContentContainingIgnoreCase(any(), any(), any());
    }

    @Test
    @DisplayName("getByConversationId search parametresi boş string ise findAllByConversationId çağrılmalı")
    void getByConversationId_WhenSearchIsEmptyString_ShouldCallFindAllByConversationId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messagePage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(messageRepository.findAllByConversationId(conversationId, pageable))
                .thenReturn(messagePage);

        // Act
        messageService.getByConversationId(conversationId, "   ", pageable);

        // Assert
        verify(messageRepository).findAllByConversationId(conversationId, pageable);
        verify(messageRepository, never())
                .findAllByConversationIdAndContentContainingIgnoreCase(any(), any(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getByConversationId – search parametresi dolu
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByConversationId search parametresi doluysa findAllByConversationIdAndContentContaining çağrılmalı")
    void getByConversationId_WhenSearchIsPresent_ShouldCallSearchRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "sipariş";
        Page<Message> messagePage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(messageRepository.findAllByConversationIdAndContentContainingIgnoreCase(
                conversationId, searchTerm, pageable))
                .thenReturn(messagePage);

        // Act
        messageService.getByConversationId(conversationId, searchTerm, pageable);

        // Assert
        verify(messageRepository).findAllByConversationIdAndContentContainingIgnoreCase(
                conversationId, searchTerm, pageable);
        verify(messageRepository, never()).findAllByConversationId(any(), any());
    }
}
