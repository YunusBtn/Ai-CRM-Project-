package com.yunus.auth.dto;

import com.yunus.auth.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(


        @NotNull(message = "Role name is required")
        Role.RoleName roleName
) {
}
