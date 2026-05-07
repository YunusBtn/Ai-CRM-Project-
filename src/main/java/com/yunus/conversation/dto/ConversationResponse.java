package com.yunus.conversation.dto;

import com.yunus.enums.ConversationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationResponse(

        UUID id,
        String tittle,
        ConversationStatus status,
        UUID customerId,
        String customerFullName,
        UUID assignedToId,
        String assignedToFullName,
        LocalDateTime lastMessageAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt


) { }
