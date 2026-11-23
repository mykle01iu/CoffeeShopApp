
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
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

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
                                "/api/auth/**"
                        ).permitAll()

                        // ==============================
                        // 2. PRODUCT API
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products",
                                "/api/products/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // ==============================
                        // 3. CATEGORY API
                        // ==============================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/categories/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categories"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // ==============================
                        // 4. ORDER API - NGAY 6
                        // ==============================

                        // Chi ADMIN xem tat ca don hang
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/admin"
                        ).hasRole("ADMIN")

                        // USER va ADMIN deu co the tao don
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders"
                        ).authenticated()

                        // USER xem don cua minh
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/my"
                        ).authenticated()

                        // ==============================
                        // 5. ORDER STATUS API - NGAY 7
                        // ==============================

                        // Chi ADMIN duoc cap nhat trang thai
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/orders/*/status"
                        ).hasRole("ADMIN")

                        // Xem lich su: can dang nhap
                        // Service se kiem tra chu don hoac ADMIN
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/*/history"
                        ).authenticated()

                        // ==============================
                        // 6. OTHER ORDER API
                        // ==============================

                        .requestMatchers(
                                "/api/orders",
                                "/api/orders/**"
                        ).authenticated()

                        // ==============================
                        // 7. OTHER API
                        // ==============================

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
