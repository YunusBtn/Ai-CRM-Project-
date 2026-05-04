package com.yunus.dto;

public record AuthResponse(

        String token,
        String email,
        String message

) {}
