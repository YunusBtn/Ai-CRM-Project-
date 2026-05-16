package com.yunus.tag.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TagResponse(

        UUID id,
        String name,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}
