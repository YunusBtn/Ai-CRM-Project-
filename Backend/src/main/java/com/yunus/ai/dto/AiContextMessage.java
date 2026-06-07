package com.yunus.ai.dto;

import java.time.LocalDateTime;

public record AiContextMessage(

        String role,


        String content,

        LocalDateTime sentAt

) {
}
