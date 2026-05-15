package com.yunus.conversation;

import com.yunus.common.PageResponse;
import com.yunus.conversation.dto.ConversationAssignRequest;
import com.yunus.conversation.dto.ConversationCreateRequest;
import com.yunus.conversation.dto.ConversationResponse;
import com.yunus.conversation.dto.ConversationStatusUpdateRequest;
import com.yunus.enums.ConversationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations", description = "Conversation endpoints")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;


    @PostMapping("/create/{customerId}")
    @Operation(summary = "Create a new conversation")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createConversation(
            @PathVariable UUID customerId,
            @RequestBody @Valid ConversationCreateRequest request) {

        return conversationService.createConversation(customerId, request);
    }


    @Operation(summary = "Get all conversations by customer ID")
    @GetMapping("/get/{customerId}")
    public PageResponse<ConversationResponse> getByCustomerId(
            @PathVariable UUID customerId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return conversationService.getByCustomerId(customerId, pageable);
    }


    @Operation(summary = "Get all conversations")
    @GetMapping("/all")
    public PageResponse<ConversationResponse> getAll(
            @RequestParam(required = false) ConversationStatus status,
            @RequestParam(required = false) UUID assignedToId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) boolean unassigned,
            Pageable pageable) {

        return conversationService.getAll(
                status,
                assignedToId,
                customerId,
                unassigned,
                pageable);
    }

    @GetMapping("/getConversation/{id}")
    @Operation(summary = "Get a conversation by ID")
    public ConversationResponse getById(@PathVariable UUID id) {
        return conversationService.getById(id);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete a conversation by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(@PathVariable UUID id) {
        conversationService.deleteConversation(id);
    }

    @PutMapping("/updateStatus/{id}")
    @Operation(summary = "Update the status of a conversation")
    public ConversationResponse updateStatus(
            @PathVariable UUID id,
            @RequestBody @Valid ConversationStatusUpdateRequest request) {
        return conversationService.updateStatus(id, request);
    }


    @PatchMapping("/assign/{id}")
    @Operation(summary = "Assign a conversation to a user")
    public ConversationResponse assign(
            @PathVariable UUID id,
            @RequestBody @Valid ConversationAssignRequest request) {
        return conversationService.assign(id, request);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my conversations")
    public PageResponse<ConversationResponse> getMyConversations(Pageable pageable) {
        return conversationService.getMyConversations(pageable);
    }


    @GetMapping("/unassigned")
    @Operation(summary = "Get unassigned conversations")
    public PageResponse<ConversationResponse> getUnassignedConversations(Pageable pageable) {
        return conversationService.getUnassignedConversations(pageable);
    }

    @GetMapping("/waitingReply")
    @Operation(summary = "Get waiting reply conversations")
    public PageResponse<ConversationResponse> getWaitingReplyConversations(Pageable pageable) {
        return conversationService.getWaitingReplyConversations(pageable);
    }

}
