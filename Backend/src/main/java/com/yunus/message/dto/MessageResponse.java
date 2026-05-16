package com.yunus.message.dto;

import com.yunus.enums.MessageDirection;
import com.yunus.enums.SenderType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(

        UUID id,
        UUID conversationId,
        String content,
        MessageDirection messageDirection,
        SenderType senderType,
        UUID senderUserId,
        String senderUserFullName,
        LocalDateTime sentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
