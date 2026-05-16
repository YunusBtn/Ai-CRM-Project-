package com.yunus.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(


        @Email(message = "Email format is invalid")
        @NotBlank(message = "Email is required")
        String email,


        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password

) {}
