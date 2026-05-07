package com.yunus.message;

import com.yunus.auth.entity.User;
import com.yunus.common.PageResponse;
import com.yunus.conversation.Conversation;
import com.yunus.conversation.ConversationRepository;
import com.yunus.conversation.ConversationService;
import com.yunus.enums.MessageDirection;
import com.yunus.enums.SenderType;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.message.dto.MessageCreateRequest;
import com.yunus.message.dto.MessageResponse;
import com.yunus.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ConversationRepository conversationRepository;

    //Message Create
    @Transactional
    public MessageResponse create(UUID conversationId, MessageCreateRequest request) {
        Conversation conversation = findActiveConversationById(conversationId);
        validateDirectionAndSenderType(request.messageDirection(), request.senderType());
        Message message = messageMapper.toEntity(request);

        message.setConversation(conversation);

        message.setSentAt(LocalDateTime.now());

        if (request.senderType() == SenderType.AGENT) {
            User currentUser = getCurrentUser();
            message.setSenderUser(currentUser);
        }

        Message savedMessage = messageRepository.save(message);
        conversation.setLastMessageAt(savedMessage.getSentAt());

        conversationRepository.save(conversation);


        return messageMapper.toResponse(savedMessage);
    }

    //Message GetByConversationId
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> getByConversationId(UUID conversationId, Pageable pageable) {

        findActiveConversationById(conversationId);

        Page<MessageResponse> messagePage = messageRepository.findAllByConversationId(conversationId, pageable)
                .map(messageMapper::toResponse);
        return PageResponse.from(messagePage);
    }


    private Conversation findActiveConversationById(UUID id) {
        return conversationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Conversation not found"));
    }


    private void validateDirectionAndSenderType(MessageDirection direction, SenderType senderType) {

        boolean inboundCustomer = direction == MessageDirection.INBOUND && senderType == SenderType.CUSTOMER;
        boolean outboundAgent = direction == MessageDirection.OUTBOUND && senderType == SenderType.AGENT;
        boolean outboundSystem = direction == MessageDirection.OUTBOUND && senderType == SenderType.SYSTEM;

        if (!(inboundCustomer || outboundAgent || outboundSystem)) {
            throw new BusinessException(ErrorType.VALIDATION_ERROR, "Invalid message direction or sender type");
        }
    }


    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorType.ACCESS_DENIED, "User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            throw new BusinessException(ErrorType.ACCESS_DENIED, "User not authenticated");
        }

        return userPrincipal.getUser();


    }


}
