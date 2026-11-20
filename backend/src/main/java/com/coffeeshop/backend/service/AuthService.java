package com.coffeeshop.backend.service;

import com.coffeeshop.backend.dto.LoginRequest;
import com.coffeeshop.backend.dto.RegisterRequest;
import com.coffeeshop.backend.entity.Role;
import com.coffeeshop.backend.entity.User;
import com.coffeeshop.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        String identifier = request.getIdentifier() == null ? "" : request.getIdentifier().trim();
        if (identifier.isEmpty()) throw new RuntimeException("Vui lòng nhập email hoặc số điện thoại");

        boolean isPhone = request.isPhone() || identifier.matches("^[0-9+]+$");
        String normalizedPhone = isPhone ? normalizePhone(identifier) : null;

        if (isPhone) {
            if (normalizedPhone == null || !normalizedPhone.matches("^0[35789][0-9]{8}$")) {
                throw new RuntimeException("Số điện thoại Việt Nam không hợp lệ");
            }
        } else {
            if (!identifier.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new RuntimeException("Email không hợp lệ");
            }
        }

        String emailValue = isPhone ? normalizedPhone + "@phone.mocmien" : identifier.toLowerCase();
        String phoneValue = isPhone ? normalizedPhone : null;

        if (isPhone) {
            if (userRepository.findByPhone(normalizedPhone).isPresent() || userRepository.existsByEmail(normalizedPhone)) {
                throw new RuntimeException("Số điện thoại này đã được đăng ký");
            }
        } else {
            if (userRepository.existsByEmail(emailValue)) {
                throw new RuntimeException("Email này đã được đăng ký");
            }
        }

        User user = User.builder()
                .name(request.getName())
                .email(emailValue)
                .phone(phoneValue)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        String normalizedPhone = normalizePhone(identifier);

        Optional<User> userOpt = userRepository.findByEmail(identifier.toLowerCase());
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByPhone(normalizedPhone);
        }
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(normalizedPhone);
        }

        User user = userOpt.orElseThrow(() -> new RuntimeException("Thông tin đăng nhập không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Thông tin đăng nhập không đúng");
        }
        return user;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String value = phone.replaceAll("[\\s-]", "");
        if (value.startsWith("+84")) return "0" + value.substring(3);
        if (value.startsWith("84") && value.length() == 11) return "0" + value.substring(2);
        return value;
    }
}
