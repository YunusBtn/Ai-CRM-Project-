package com.yunus.note;


import com.yunus.auth.entity.User;
import com.yunus.common.PageResponse;
import com.yunus.conversation.Conversation;
import com.yunus.conversation.ConversationRepository;
import com.yunus.customer.Customer;
import com.yunus.customer.CustomerRepository;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.note.dto.NoteCreateRequest;
import com.yunus.note.dto.NoteResponse;
import com.yunus.note.dto.NoteUpdateRequest;
import com.yunus.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final CustomerRepository customerRepository;
    private final ConversationRepository conversationRepository;
    private final NoteMapper noteMapper;

    @Transactional
    public NoteResponse createCustomerNote(UUID customerId, NoteCreateRequest request) {
        Customer customer = findActiveCustomerById(customerId);
        User currentUser = getCurrentUser();

        Note note = noteMapper.toEntity(request);
        note.setCustomer(customer);
        note.setConversation(null);
        note.setCreatedBy(currentUser);

        Note savedNote = noteRepository.save(note);

        return noteMapper.toResponse(savedNote);
    }

    @Transactional
    public NoteResponse createConversationNote(UUID conversationId, NoteCreateRequest request) {
        Conversation conversation = findActiveConversationById(conversationId);
        User currentUser = getCurrentUser();

        Note note = noteMapper.toEntity(request);
        note.setConversation(conversation);
        note.setCustomer(null);
        note.setCreatedBy(currentUser);

        Note savedNote = noteRepository.save(note);

        return noteMapper.toResponse(savedNote);
    }

    @Transactional(readOnly = true)
    public NoteResponse getById(UUID id) {
        Note note = findNoteById(id);

        return noteMapper.toResponse(note);
    }

    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> getByCustomerId(UUID customerId, Pageable pageable) {
        findActiveCustomerById(customerId);

        Page<NoteResponse> notePage = noteRepository.findAllByCustomerId(customerId, pageable)
                .map(noteMapper::toResponse);

        return PageResponse.from(notePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> getByConversationId(UUID conversationId, Pageable pageable) {
        findActiveConversationById(conversationId);

        Page<NoteResponse> notePage = noteRepository.findAllByConversationId(conversationId, pageable)
                .map(noteMapper::toResponse);

        return PageResponse.from(notePage);
    }

    @Transactional
    public NoteResponse update(UUID id, NoteUpdateRequest request) {
        Note note = findNoteById(id);
        User currentUser = getCurrentUser();

        if (!note.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorType.ACCESS_DENIED,"Bu notu güncelleme yetkiniz yok.");
        }
        note.setContent(request.content());

        Note updatedNote = noteRepository.save(note);

        return noteMapper.toResponse(updatedNote);
    }

    @Transactional
    public void delete(UUID id) {
        Note note = findNoteById(id);
        User currentUser = getCurrentUser();

        if (!note.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorType.ACCESS_DENIED, "Bu notu silme yetkiniz yok.");
        }
        noteRepository.delete(note);
    }

    private Note findNoteById(UUID id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Note not found"));
    }

    private Customer findActiveCustomerById(UUID id) {
        return customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Customer not found"));
    }

    private Conversation findActiveConversationById(UUID id) {
        return conversationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Conversation not found"));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorType.ACCESS_DENIED, "Authenticated user not found");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal)) {
            throw new BusinessException(ErrorType.ACCESS_DENIED, "Invalid authenticated user");
        }

        return userPrincipal.getUser();
    }
}