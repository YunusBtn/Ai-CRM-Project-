package com.yunus.message;

import com.yunus.common.ApiResponse;
import com.yunus.common.PageResponse;
import com.yunus.message.dto.MessageCreateRequest;
import com.yunus.message.dto.MessageResponse;
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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Message endpoints")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/create/{conversationId}")
    @Operation(summary = "Create a new message")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MessageResponse> create(
            @PathVariable UUID conversationId,
            @Valid @RequestBody MessageCreateRequest request) {

        return ApiResponse.success(messageService.create(conversationId, request));
    }

    @GetMapping("/get/{conversationId}")
    @Operation(summary = "Get messages by conversation ID")
    public ApiResponse<PageResponse<MessageResponse>> getByConversationId(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "sentAt") Pageable pageable) {
        return ApiResponse.success(messageService.getByConversationId(conversationId, search, pageable));

    }


}
