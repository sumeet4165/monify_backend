package com.example.MONEYMANAGER.middleware;

import com.example.MONEYMANAGER.service.CustomUserDetailsService;
import com.example.MONEYMANAGER.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Step 1: Check if header is missing or malformed
        if (authHeader == null || !authHeader.trim().toLowerCase().startsWith("bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Extract token
        String token = authHeader.trim().substring(7).trim();

        // Step 3: Extract email from token
        String email = jwtUtil.getUsernameFromToken(token);

        // Step 4: Validate token and set authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    List.of() // empty authorities
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                logger.error("Could not set user authentication", e);
            }
        }

        // Step 5: Continue filter chain
        filterChain.doFilter(request, response);
    }
}
