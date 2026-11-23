package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.UpdateProfileRequest;
import com.coffeeshop.backend.dto.UserProfileResponse;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.repository.UserRepository;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. Lay thong tin nguoi dung dang dang nhap
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(
            Authentication authentication) {

        User user = findCurrentUser(authentication);

        return toResponse(user);
    }

    // 2. Cap nhat ten nguoi dung
    @Transactional
    public UserProfileResponse updateMyProfile(
            Authentication authentication,
            UpdateProfileRequest request) {

        User user = findCurrentUser(authentication);

        if (request == null || request.name() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ten khong duoc de trong"
            );
        }

        String newName = request.name().trim();

        if (newName.isEmpty() || newName.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ten phai co tu 1 den 100 ky tu"
            );
        }

        user.setName(newName);

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    // 3. ADMIN lay danh sach nguoi dung
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers(
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chi ADMIN duoc xem danh sach nguoi dung"
            );
        }

        return userRepository
                .findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 4. Tim nguoi dung theo email trong JWT
    private User findCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Can dang nhap"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Khong tim thay nguoi dung"
                        )
                );
    }

    // 5. Kiem tra quyen ADMIN
    private boolean isAdmin(Authentication authentication) {

        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        );
    }

    // 6. Chuyen User thanh DTO, khong tra password
    private UserProfileResponse toResponse(User user) {

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
