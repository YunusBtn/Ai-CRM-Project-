package com.yunus.note.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteResponse(

        UUID id,
        UUID customerId,
        String content,
        UUID conversationId,
        UUID createdId,
        String createdByFullName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt


) {
}
