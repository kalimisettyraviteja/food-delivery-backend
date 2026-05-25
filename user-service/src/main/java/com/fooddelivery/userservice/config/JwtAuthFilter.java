package com.fooddelivery.userservice.config;


import com.fooddelivery.userservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    //private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1 — Read Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2 — Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // no token → skip
            return;
        }

        // Step 3 — Extract token
        String token = authHeader.substring(7);

        // Step 4 — Validate token
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response); // invalid token → skip
            return;
        }

        // Step 5 — Extract claims from token
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        // Step 6 — Set Authentication into SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("✅ JWT authenticated → {} | Role → {}", email, role);

        // Step 7 — Continue the request
        filterChain.doFilter(request, response);
    }
}