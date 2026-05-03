package com.trackingpath.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.trackingpath.security.CustomDriverUserPrincipal;
import com.trackingpath.security.CustomUserPrincipal;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserPrincipal) authentication.getPrincipal();
    }
 // 🔹 For Driver
    public static CustomDriverUserPrincipal getCurrentDriver() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomDriverUserPrincipal) {
            return (CustomDriverUserPrincipal) principal;
        }

        throw new RuntimeException("Current user is not a Driver");
    }
}
