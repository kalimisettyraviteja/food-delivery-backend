package com.fooddelivery.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/ping",
                                "/api/users/auth/email-status",
                                "/api/users/register",
                                "/api/users/manager/register-request",
                                "/api/users/verify-email",
                                "/api/users/resend-verification",
                                "/api/users/login",
                                "/api/users/forgot-password",
                                "/api/users/verify-reset-otp",
                                "/api/users/reset-password",
                                "/api/users/reactivate/request",
                                "/api/users/reactivate/confirm"
                        ).permitAll()
                        .requestMatchers(
                                "/api/users",
                                "/api/users/managers",
                                "/api/users/manager-requests",
                                "/api/users/manager-requests/**"
                        ).hasAuthority("ADMIN")
                        .requestMatchers(
                                "/api/users/profile",
                                "/api/users/profile/change-password",
                                "/api/users/profile/photo",
                                "/api/users/profile/deactivate/request",
                                "/api/users/profile/deactivate/confirm",
                                "/api/users/addresses",
                                "/api/users/addresses/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}