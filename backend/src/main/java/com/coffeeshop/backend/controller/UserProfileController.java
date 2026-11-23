
package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.UpdateProfileRequest;
import com.coffeeshop.backend.dto.UserProfileResponse;
import com.coffeeshop.backend.service.UserProfileService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(
            UserProfileService userProfileService) {

        this.userProfileService = userProfileService;
    }

    // USER va ADMIN xem profile cua minh
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                userProfileService.getMyProfile(authentication)
        );
    }

    // USER va ADMIN cap nhat ten ca nhan
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(
                userProfileService.updateMyProfile(
                        authentication,
                        request
                )
        );
    }

    // Chi ADMIN duoc xem danh sach nguoi dung
    @GetMapping("/admin")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(
            Authentication authentication) {

        return ResponseEntity.ok(
                userProfileService.getAllUsers(authentication)
        );
    }
}
