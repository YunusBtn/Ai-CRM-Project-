package com.yunus.conversation.dto;

import com.yunus.enums.ConversationStatus;
import jakarta.validation.constraints.NotNull;

public record ConversationStatusUpdateRequest(


        @NotNull(message = "Status is required")
        ConversationStatus status



) {
}
