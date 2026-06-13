package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.dto.request.ChangePasswordRequest;
import com.ayoub.budgettracker.dto.request.UpdateProfileRequest;
import com.ayoub.budgettracker.dto.response.UserProfileResponse;
import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User user) {
        userService.changePassword(user, request);
        return ResponseEntity.noContent().build();
    }
}
