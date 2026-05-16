package com.yunus.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagCreateRequest(

        @NotBlank(message = "Tag name is required")
        @Size(min = 2, message = "Tag name must be at least 2 characters long")
        String name,


        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
                message = "Color must be a valid hex color code. Example: #FF1453")
        String color


) {
}
