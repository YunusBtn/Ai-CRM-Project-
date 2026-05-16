package com.yunus.dashboard.dto;

import com.yunus.enums.ConversationStatus;

public record ConversationStatusCountResponse(
        ConversationStatus status,
        long count


) {
}
