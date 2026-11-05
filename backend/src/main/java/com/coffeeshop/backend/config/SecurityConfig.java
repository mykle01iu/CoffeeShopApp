
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
                // REST API su dung JWT, khong dung CSRF token
                .csrf(csrf -> csrf.disable())

                // Khong luu session tren server
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
                        // 4. OTHER API
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
