package com.yunus.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteUpdateRequest(


        @NotBlank(message = "Content is required")
        @Size(min = 1, max = 2000, message = "Content must be at least 1 character long")
        String content


) {
}
