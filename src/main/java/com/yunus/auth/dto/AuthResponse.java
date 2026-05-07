package com.yunus.auth.dto;

public record AuthResponse(

        String token,
        String email,
        String message

) {}
