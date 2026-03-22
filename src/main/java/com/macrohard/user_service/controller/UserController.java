package com.macrohard.user_service.controller;


import com.macrohard.user_service.dto.*;
import com.macrohard.user_service.model.User;
import com.macrohard.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin (origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Email already registered")) {
                return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
            }
            return ResponseEntity.status(500).body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(userService.login(request));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
            }
            if (e.getMessage().equals("Invalid password")) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
            }
            return ResponseEntity.status(500).body(Map.of("error", "Login failed"));
        }
    }

    @GetMapping("/auth/validateToken")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(userService.validateToken(token));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running");
    }


    //user routes
    @GetMapping("/users/{id}/profile")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request) {
        try {
            return ResponseEntity.ok(userService.updateProfile(id, request));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            if (e.getMessage().equals("Email already in use")) {
                return ResponseEntity.status(409).body(Map.of("error", "Email already in use"));
            }
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update profile"));
        }
    }

    @PutMapping("/users/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(id, request);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            if (e.getMessage().equals("Current password is incorrect")) {
                return ResponseEntity.status(401).body(Map.of("error", "Current password is incorrect"));
            }
            if (e.getMessage().equals("New passwords do not match")) {
                return ResponseEntity.status(400).body(Map.of("error", "New passwords do not match"));
            }
            if (e.getMessage().equals("New password must be at least 6 characters")) {
                return ResponseEntity.status(400).body(Map.of("error", "New password must be at least 6 characters"));
            }
            return ResponseEntity.status(500).body(Map.of("error", "Failed to change password"));
        }
    }


}

