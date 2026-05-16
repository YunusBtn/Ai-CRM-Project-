package com.yunus.conversation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConversationAssignRequest(

        @NotNull(message = "Assigned to id is required")
        UUID assignedToId

) {
}
