package com.yunus.auth;

import com.yunus.auth.dto.UserRoleUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthService authService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{userId}/role")
    public void updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        authService.updateUserRole(userId, request);
    }
}
