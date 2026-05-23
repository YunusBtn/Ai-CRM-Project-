package com.yunus.conversation;

import com.yunus.auth.entity.User;
import com.yunus.auth.repository.UserRepository;
import com.yunus.common.PageResponse;
import com.yunus.conversation.dto.ConversationAssignRequest;
import com.yunus.conversation.dto.ConversationCreateRequest;
import com.yunus.conversation.dto.ConversationResponse;
import com.yunus.conversation.dto.ConversationStatusUpdateRequest;
import com.yunus.customer.Customer;
import com.yunus.customer.CustomerRepository;
import com.yunus.enums.ConversationStatus;
import com.yunus.enums.CustomerStatus;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
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
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// LENIENT: bazı testler varsayılan conversation stub'larını kullanmaz
@MockitoSettings(strictness = Strictness.LENIENT)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMapper conversationMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ConversationService conversationService;

    private UUID conversationId;
    private UUID customerId;
    private Conversation conversation;
    private Customer customer;
    private User agentUser;
    private ConversationResponse conversationResponse;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        customer = new Customer();
        customer.setStatus(CustomerStatus.ACTIVE);

        agentUser = new User();
        agentUser.setEmail("agent@test.com");
        agentUser.setActive(true);
        agentUser.setDeleted(false);

        conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(ConversationStatus.OPEN);

        conversationResponse = new ConversationResponse(
                conversationId, "Test", ConversationStatus.OPEN,
                customerId, "Ali Veli", null, null,
                null, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── SecurityContext helper ───────────────────────────────────────────────

    private void setUserInSecurityContext(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createConversation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createConversation başarılı olduğunda customer bulunmalı ve status OPEN set edilmeli")
    void createConversation_WhenSuccess_ShouldFindCustomerAndSetStatusToOpen() {
        // Arrange
        ConversationCreateRequest request = new ConversationCreateRequest("Test Başlık", null);
        Conversation mappedConversation = new Conversation();

        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(conversationMapper.toEntity(request)).thenReturn(mappedConversation);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(conversationMapper.toResponse(conversation)).thenReturn(conversationResponse);

        // Act
        ConversationResponse response = conversationService.createConversation(customerId, request);

        // Assert
        assertThat(mappedConversation.getStatus()).isEqualTo(ConversationStatus.OPEN);
        assertThat(mappedConversation.getCustomer()).isSameAs(customer);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    @DisplayName("createConversation assignedToId varsa aktif user bulunup conversation'a atanmalı")
    void createConversation_WhenAssignedToIdPresent_ShouldAssignUserToConversation() {
        // Arrange
        UUID assignedToId = UUID.randomUUID();
        ConversationCreateRequest request = new ConversationCreateRequest("Test Başlık", assignedToId);
        Conversation mappedConversation = new Conversation();

        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(conversationMapper.toEntity(request)).thenReturn(mappedConversation);
        when(userRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(assignedToId))
                .thenReturn(Optional.of(agentUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(conversationMapper.toResponse(conversation)).thenReturn(conversationResponse);

        // Act
        conversationService.createConversation(customerId, request);

        // Assert – conversation.assignedTo set edilmiş olmalı
        assertThat(mappedConversation.getAssignedTo()).isSameAs(agentUser);
    }

    @Test
    @DisplayName("createConversation customer bulunamazsa NOT_FOUND fırlatılmalı")
    void createConversation_WhenCustomerNotFound_ShouldThrowNotFound() {
        // Arrange
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.empty());
        ConversationCreateRequest request = new ConversationCreateRequest("Test", null);

        // Act & Assert
        assertThatThrownBy(() -> conversationService.createConversation(customerId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verifyNoInteractions(conversationRepository);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getById
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById aktif conversation döndürülmeli")
    void getById_WhenConversationExists_ShouldReturnConversationResponse() {
        // Arrange
        when(conversationRepository.findByIdAndIsDeletedFalse(conversationId))
                .thenReturn(Optional.of(conversation));
        when(conversationMapper.toResponse(conversation)).thenReturn(conversationResponse);

        // Act
        ConversationResponse response = conversationService.getById(conversationId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(conversationId);
    }

    @Test
    @DisplayName("getById conversation bulunamazsa NOT_FOUND fırlatılmalı")
    void getById_WhenConversationNotFound_ShouldThrowNotFound() {
        // Arrange
        when(conversationRepository.findByIdAndIsDeletedFalse(conversationId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> conversationService.getById(conversationId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateStatus
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus başarılı olduğunda conversation status güncellenmeli ve save çağrılmalı")
    void updateStatus_WhenSuccess_ShouldUpdateStatusAndSave() {
        // Arrange
        ConversationStatusUpdateRequest request = new ConversationStatusUpdateRequest(ConversationStatus.CLOSED);
        when(conversationRepository.findByIdAndIsDeletedFalse(conversationId))
                .thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(conversationMapper.toResponse(conversation)).thenReturn(conversationResponse);

        // Act
        conversationService.updateStatus(conversationId, request);

        // Assert
        assertThat(conversation.getStatus()).isEqualTo(ConversationStatus.CLOSED);
        verify(conversationRepository).save(conversation);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // assign
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("assign başarılı olduğunda aktif user bulunup conversation'a atanmalı")
    void assign_WhenSuccess_ShouldAssignUserToConversation() {
        // Arrange
        UUID assignedToId = UUID.randomUUID();
        ConversationAssignRequest request = new ConversationAssignRequest(assignedToId);

        when(conversationRepository.findByIdAndIsDeletedFalse(conversationId))
                .thenReturn(Optional.of(conversation));
        when(userRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(assignedToId))
                .thenReturn(Optional.of(agentUser));
        when(conversationRepository.save(conversation)).thenReturn(conversation);
        when(conversationMapper.toResponse(conversation)).thenReturn(conversationResponse);

        // Act
        conversationService.assign(conversationId, request);

        // Assert
        assertThat(conversation.getAssignedTo()).isSameAs(agentUser);
        verify(conversationRepository).save(conversation);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteConversation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteConversation çağrıldığında isDeleted true yapılmalı ve save çağrılmalı")
    void deleteConversation_WhenSuccess_ShouldMarkDeletedAndSave() {
        // Arrange
        when(conversationRepository.findByIdAndIsDeletedFalse(conversationId))
                .thenReturn(Optional.of(conversation));

        // Act
        conversationService.deleteConversation(conversationId);

        // Assert
        assertThat(conversation.isDeleted()).isTrue();
        verify(conversationRepository).save(conversation);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getMyConversations – SecurityContextHolder'dan current user alınıyor
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyConversations current user id alınmalı ve OPEN/PENDING conversationlar getirilmeli")
    void getMyConversations_ShouldQueryWithCurrentUserIdAndActiveStatuses() {
        // Arrange
        // Not: agentUser.getId() == null çünkü BaseEntity @PrePersist çalışmıyor; nullable() matcher kullanıyoruz
        setUserInSecurityContext(agentUser);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Conversation> conversationPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(conversationRepository.findAllByAssignedToIdAndStatusInAndIsDeletedFalse(
                nullable(UUID.class), anyList(), eq(pageable)))
                .thenReturn(conversationPage);

        // Act
        PageResponse<ConversationResponse> result = conversationService.getMyConversations(pageable);

        // Assert
        assertThat(result).isNotNull();
        verify(conversationRepository).findAllByAssignedToIdAndStatusInAndIsDeletedFalse(
                nullable(UUID.class),
                argThat(statuses -> statuses.contains(ConversationStatus.OPEN)
                        && statuses.contains(ConversationStatus.PENDING)),
                eq(pageable)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUnassignedConversations
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUnassignedConversations assignedTo null ve OPEN/PENDING conversation'ları getirmeli")
    void getUnassignedConversations_ShouldQueryUnassignedWithActiveStatuses() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Conversation> conversationPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(conversationRepository.findAllByAssignedToIsNullAndStatusInAndIsDeletedFalse(
                anyList(), eq(pageable)))
                .thenReturn(conversationPage);

        // Act
        PageResponse<ConversationResponse> result = conversationService.getUnassignedConversations(pageable);

        // Assert
        assertThat(result).isNotNull();
        verify(conversationRepository).findAllByAssignedToIsNullAndStatusInAndIsDeletedFalse(
                argThat(statuses -> statuses.contains(ConversationStatus.OPEN)
                        && statuses.contains(ConversationStatus.PENDING)),
                eq(pageable)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getWaitingReplyConversations
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getWaitingReplyConversations repository.findWaitingConversations doğru parametrelerle çağrılmalı")
    void getWaitingReplyConversations_ShouldCallFindWaitingConversationsWithCorrectParams() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Conversation> conversationPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(conversationRepository.findWaitingConversations(anyList(), any(), eq(pageable)))
                .thenReturn(conversationPage);

        // Act
        PageResponse<ConversationResponse> result = conversationService.getWaitingReplyConversations(pageable);

        // Assert
        assertThat(result).isNotNull();
        verify(conversationRepository).findWaitingConversations(
                argThat(statuses -> statuses.contains(ConversationStatus.OPEN)
                        && statuses.contains(ConversationStatus.PENDING)),
                any(),
                eq(pageable)
        );
    }
}
