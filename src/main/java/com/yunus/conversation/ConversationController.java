package com.yunus.conversation;

import com.yunus.common.PageResponse;
import com.yunus.conversation.dto.ConversationAssignRequest;
import com.yunus.conversation.dto.ConversationCreateRequest;
import com.yunus.conversation.dto.ConversationResponse;
import com.yunus.conversation.dto.ConversationStatusUpdateRequest;
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
    public PageResponse<ConversationResponse> getAll(Pageable pageable) {
        return conversationService.getAll(pageable);
    }

    @GetMapping("/getConversation/{id}")
    @Operation(summary = "Get a conversation by ID")
    public ConversationResponse getById(UUID id) {
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


}
