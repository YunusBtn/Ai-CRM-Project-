package com.yunus.conversation.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConversationCreateRequest(

        @Size(max = 255, message = "Tittle must be less than 255 characters")
        String tittle,

        UUID assignedToId

) {
}
