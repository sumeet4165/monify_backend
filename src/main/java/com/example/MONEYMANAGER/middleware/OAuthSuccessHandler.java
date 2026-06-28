package com.example.MONEYMANAGER.middleware;

import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.repository.ProfileRepository;
import com.example.MONEYMANAGER.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final ProfileRepository profileRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = (String) oAuth2User.getAttributes().get("email");
        if (email == null && oAuth2User.getAttributes().containsKey("login")) {
            email = oAuth2User.getAttribute("login") + "@github.com";
        }

        String name = (String) oAuth2User.getAttributes().get("name");
        if (name == null && email != null) {
            name = email.split("@")[0];
        } else if (name == null) {
            name = "OAuth User";
        }

        // Ensure user exists
        if (profileRepository.findByEmail(email).isEmpty()) {
            ProfileEntity newProfile = ProfileEntity.builder()
                    .email(email)
                    .fullname(name)
                    .password("") // OAuth users don't need a password
                    .isactive(true) // Auto-activate
                    .build();
            profileRepository.save(newProfile);
        }

        // Generate JWT for this user
        String token = jwtUtil.generateAccessToken(email);

        // Redirect to frontend with token
        response.sendRedirect("http://localhost:5173/dashboard?token=" + token);
    }
}
