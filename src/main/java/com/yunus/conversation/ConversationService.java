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
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    //Conversation Create
    @Transactional
    public ConversationResponse createConversation(UUID customerId, ConversationCreateRequest request) {


        Customer customer = findActiveCustomerById(customerId);
        Conversation conversation = conversationMapper.toEntity(request);
        conversation.setCustomer(customer);


        if (request.assignedToId() != null) {
            User assignedUser = findActiveById(request.assignedToId());
            conversation.setAssignedTo(assignedUser);
        }

        Conversation savedConversation = conversationRepository.save(conversation);

        return conversationMapper.toResponse(savedConversation);

    }


    //Conversation GetById
    @Transactional(readOnly = true)
    public ConversationResponse getById(UUID id) {

        Conversation conversation = findActiveConversationById(id);
        return conversationMapper.toResponse(conversation);

    }

    //Conversation getByCustomerId
    @Transactional(readOnly = true)
    public PageResponse<ConversationResponse> getByCustomerId(UUID customerId, Pageable pageable) {

        findActiveCustomerById(customerId);

        Page<ConversationResponse> conversationPage =
                conversationRepository.findAllByCustomerIdAndIsDeletedFalse(customerId, pageable)
                        .map(conversationMapper::toResponse);

        return PageResponse.from(conversationPage);
    }

    //Conversation Update Status
    @Transactional
    public ConversationResponse updateStatus(UUID id, ConversationStatusUpdateRequest request) {

        Conversation conversation = findActiveConversationById(id);
        conversation.setStatus(request.status());

        return conversationMapper.toResponse(conversationRepository.save(conversation));
    }


    //Conversation AssignTo
    @Transactional
    public ConversationResponse assign(UUID id, ConversationAssignRequest request) {
        Conversation conversation = findActiveConversationById(id);

        User assignedUser = findActiveById(request.assignedToId());
        conversation.setAssignedTo(assignedUser);

        Conversation updatedConversation = conversationRepository.save(conversation);
        return conversationMapper.toResponse(updatedConversation);

    }

    //Conversation Delete
    @Transactional
    public void deleteConversation(UUID id) {
        Conversation conversation = findActiveConversationById(id);
        conversation.setDeleted(true);
        conversationRepository.save(conversation);
    }

    //Conversation GetAll
    @Transactional(readOnly = true)
    public PageResponse<ConversationResponse> getAll(Pageable pageable) {
        Page<ConversationResponse> conversationPage = conversationRepository.findAllByIsDeletedFalse(pageable)
                .map(conversationMapper::toResponse);
        return PageResponse.from(conversationPage);
    }

    private Conversation findActiveConversationById(UUID id) {
        return conversationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Conversation not found"));
    }

    private Customer findActiveCustomerById(UUID id) {
        return customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Customer not found"));
    }

    private User findActiveById(UUID id) {
        return userRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "User not found"));
    }


}
