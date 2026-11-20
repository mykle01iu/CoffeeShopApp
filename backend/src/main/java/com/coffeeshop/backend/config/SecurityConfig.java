
package com.coffeeshop.backend.config;

import com.coffeeshop.backend.security.JwtAuthFilter;
import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // REST API su dung JWT, khong dung CSRF token
                .csrf(csrf -> csrf.disable())

                // Khong luu session tren server
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(errors -> errors.authenticationEntryPoint(
                        new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)))

                .authorizeHttpRequests(auth -> auth

                        // Cho phep request noi bo cua Spring
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR,
                                DispatcherType.FORWARD
                        ).permitAll()

                        // ==============================
                        // 1. PUBLIC API
                        // ==============================

                        .requestMatchers(
                                "/api/health",
                                "/api/auth/**",
                                "/images/products/**"
                        ).permitAll()

                        // Don hang va lich su don can JWT hop le
                        .requestMatchers("/api/orders", "/api/orders/**").authenticated()

                        // ==============================
                        // 2. PRODUCT API
                        // ==============================

                        // Tat ca duoc xem san pham
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products",
                                "/api/products/**"
                        ).permitAll()

                        // Chi ADMIN duoc them san pham
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // Chi ADMIN duoc sua san pham
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // Chi ADMIN duoc xoa san pham
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // ==============================
                        // 3. CATEGORY API
                        // ==============================

                        // Tat ca duoc xem danh muc
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/categories/**"
                        ).permitAll()

                        // Chi ADMIN duoc them danh muc
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categories"
                        ).hasRole("ADMIN")

                        // Chi ADMIN duoc sua danh muc
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // Chi ADMIN duoc xoa danh muc
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // ==============================
                        // 4. ORDER API - NGAY 6
                        // ==============================

                        // Chi ADMIN duoc xem tat ca don hang
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/admin"
                        ).hasRole("ADMIN")

                        // USER va ADMIN deu co the tao don
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders"
                        ).authenticated()

                        // USER va ADMIN xem don cua minh
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/my"
                        ).authenticated()

                        // Cac Order API khac yeu cau dang nhap
                        .requestMatchers(
                                "/api/orders",
                                "/api/orders/**"
                        ).authenticated()

                        // ==============================
                        // 5. OTHER API
                        // ==============================

                        // API con lai bat buoc dang nhap
                        .anyRequest().authenticated()
                )

                // Kiem tra JWT truoc filter dang nhap
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
