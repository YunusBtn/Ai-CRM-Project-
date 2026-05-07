package com.yunus.customer.dto;

import com.yunus.enums.CustomerStatus;
import com.yunus.tag.dto.TagResponse;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record CustomerResponse(

        UUID id,
        String firstName,
        String lastName,
        String phone,
        String email,
        CustomerStatus status,
        Set<TagResponse> tags,
        LocalDateTime createdAt,
        LocalDateTime uptadedAt


) { }
