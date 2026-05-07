package com.yunus.message;

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
    public MessageResponse create(
            @PathVariable UUID conversationId,
            @Valid @RequestBody MessageCreateRequest request) {

        return messageService.create(conversationId, request);
    }

    @GetMapping("/get/{conversationId}")
    @Operation(summary = "Get messages by conversation ID")
    public PageResponse<MessageResponse> getByConversationId(
            @PathVariable UUID conversationId,
            @PageableDefault(size = 10, sort = "sendAt") Pageable pageable) {
        return messageService.getByConversationId(conversationId, pageable);

    }


}
