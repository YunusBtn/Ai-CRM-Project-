package com.yunus.dashboard.dto;

import java.util.UUID;

public record TagCustomerCountResponse(

        UUID tagId,
        String tagName,
        long customerCount

) {
}
