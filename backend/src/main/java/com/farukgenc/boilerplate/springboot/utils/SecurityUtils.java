package com.farukgenc.boilerplate.springboot.utils;

import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Utility class for security-related operations
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserService userService;
    private static SecurityUtils instance;

    @PostConstruct
    private void init() {
        instance = this;
    }

    /**
     * Get the currently authenticated user
     * @return The authenticated User object
     * @throws IllegalStateException if no user is authenticated
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        
        // Handle Spring Security UserDetails principal
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = instance.userService.findByUsername(username);
            if (user == null) {
                throw new IllegalStateException("User not found: " + username);
            }
            return user;
        }
        
        // Handle direct User entity principal (legacy support)
        if (principal instanceof User) {
            return (User) principal;
        }

        throw new IllegalStateException("Authenticated principal is not of supported type: " + principal.getClass().getSimpleName());
    }

    /**
     * Get the username of the currently authenticated user
     * @return The username of the authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        }

        throw new IllegalStateException("Authenticated principal is not of supported type: " + principal.getClass().getSimpleName());
    }

    /**
     * Check if a user is authenticated
     * @return true if a user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
               (authentication.getPrincipal() instanceof UserDetails || authentication.getPrincipal() instanceof User);
    }
}
