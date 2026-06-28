package com.example.MONEYMANAGER.service;

import com.example.MONEYMANAGER.dto.AuthDto;
import com.example.MONEYMANAGER.dto.ProfileDto;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.mapper.ProfileMapper;
import com.example.MONEYMANAGER.repository.ProfileRepository;
import com.example.MONEYMANAGER.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ProfileDto register(ProfileDto profileDto) {
        ProfileEntity newprofile = profileMapper.toEntity(profileDto);
        newprofile.setPassword(passwordEncoder.encode(newprofile.getPassword()));
        newprofile.setActivationtoken(UUID.randomUUID().toString());

        profileRepository.save(newprofile);

        // Send activation email
        String activationLink = "http://localhost:8080/api/v1/activate?token=" + newprofile.getActivationtoken();
        String subject = "✨ Activate Your MoneyManager Account";

        // HTML email body
        String body = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #f7f9fc;
                    margin: 0;
                    padding: 0;
                }
                .container {
                    background-color: #ffffff;
                    border-radius: 10px;
                    max-width: 600px;
                    margin: 40px auto;
                    padding: 30px;
                    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #007bff;
                    padding-bottom: 10px;
                }
                .header h2 {
                    color: #007bff;
                    margin: 0;
                }
                .content {
                    padding-top: 20px;
                    text-align: center;
                    color: #333;
                }
                .content p {
                    font-size: 16px;
                    margin-bottom: 30px;
                }
                .button {
                    display: inline-block;
                    background-color: #007bff;
                    color: white !important;
                    text-decoration: none;
                    padding: 12px 24px;
                    border-radius: 6px;
                    font-weight: bold;
                    letter-spacing: 0.5px;
                }
                .footer {
                    margin-top: 40px;
                    text-align: center;
                    font-size: 13px;
                    color: #888;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2>Welcome to MoneyManager 🎉</h2>
                </div>
                <div class="content">
                    <p>Hi %s,</p>
                    <p>Thank you for signing up! To activate your account and start managing your finances, please confirm your email address by clicking the button below.</p>
                    <a href="%s" class="button">Activate My Account</a>
                    <p>If the button doesn’t work, copy and paste this link into your browser:</p>
                    <p><a href="%s">%s</a></p>
                </div>
                <div class="footer">
                    <p>© 2025 MoneyManager. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(newprofile.getFullname(), activationLink, activationLink, activationLink);

        emailService.sendEmail(newprofile.getEmail(), subject, body);
        return profileMapper.toDto(newprofile);
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationtoken(activationToken)
                .map(profile -> {
                    profile.setIsactive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email).map(ProfileEntity::getIsactive).orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return profileRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
    }

    public ProfileDto getPublicProfile(String email) {
        ProfileEntity curruser;
        if (email == null) {
            curruser = getCurrentProfile();
        } else {
            curruser = profileRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        }
        return profileMapper.toDto(curruser);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDto authDto) {
        try {
            // 1️⃣ Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword())
            );

            // 2️⃣ Generate JWT Token using email
            String token = jwtUtil.generateAccessToken(authDto.getEmail());

            // 3️⃣ Return token and user info
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDto.getEmail())
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    public void deleteProfile(String email) {
        ProfileEntity user = profileRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        profileRepository.delete(user);
    }
}
