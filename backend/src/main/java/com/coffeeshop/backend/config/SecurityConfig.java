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

                        // Cho phép các request forward/error nội bộ của Spring
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR,
                                DispatcherType.FORWARD
                        ).permitAll()

                        // API công khai
                        .requestMatchers(
                                "/api/health",
                                "/api/auth/**"
                        ).permitAll()

                        // Ai cũng được xem sản phẩm
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products",
                                "/api/products/**"
                        ).permitAll()

                        // Chỉ ADMIN được thêm sản phẩm
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // Chỉ ADMIN được sửa sản phẩm
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // Chỉ ADMIN được xóa sản phẩm
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products",
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // API còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}