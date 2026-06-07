package com.yunus.auth;

import com.yunus.auth.dto.AuthResponse;
import com.yunus.auth.dto.LoginRequest;
import com.yunus.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @ApiResponse(description = "User registered successfully", responseCode = "200")
    @ApiResponse(description = "User already exists", responseCode = "409")
    @PostMapping("/register")
    public com.yunus.common.ApiResponse<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return com.yunus.common.ApiResponse.success(authService.register(request));
    }

    @ApiResponse(description = "User logged in successfully", responseCode = "200")
    @ApiResponse(description = "Invalid credentials", responseCode = "401")
    @Operation(summary = "Login a user")
    @PostMapping("/login")
    public com.yunus.common.ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return com.yunus.common.ApiResponse.success(authService.login(request));
    }


}
