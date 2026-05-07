package com.yunus.message.dto;

import com.yunus.enums.MessageDirection;
import com.yunus.enums.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageCreateRequest(
        @NotBlank(message = "Content is required")
        @NotNull(message = "Conversation id is required")
        @Size(min = 1,max = 5200, message = "Content must be at least 1 character long")
        String content,

        @NotNull(message = "Message direction is required")
        MessageDirection messageDirection,

        @NotNull(message = "Sender type is required")
        SenderType senderType




) {
}
