package com.yunus.security;

import com.yunus.auth.entity.User;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();


        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorType.ACCESS_DENIED, "User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            throw new BusinessException(ErrorType.ACCESS_DENIED, "User not authenticated");
        }
        return userPrincipal.getUser();
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

}
