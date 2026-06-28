package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.dto.AuthDto;
import com.example.MONEYMANAGER.dto.ProfileDto;
import com.example.MONEYMANAGER.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDto> register(@Valid @RequestBody ProfileDto profileDto) {
        return ResponseEntity.ok(profileService.register(profileDto));
    }

    @GetMapping(value = "/activate", produces = org.springframework.http.MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateProfile(token);
        String htmlTemplate = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Account Activation</title><style>body{font-family:'Inter',system-ui,-apple-system,sans-serif;background-color:#0f172a;color:#f8fafc;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}.card{background:rgba(30,41,59,0.7);backdrop-filter:blur(10px);border:1px solid rgba(255,255,255,0.1);border-radius:24px;padding:40px;text-align:center;max-width:400px;box-shadow:0 25px 50px -12px rgba(0,0,0,0.5)}.icon{width:80px;height:80px;margin:0 auto 20px;border-radius:50%;display:flex;align-items:center;justify-content:center}.success .icon{background:rgba(16,185,129,0.1);color:#10b981}.error .icon{background:rgba(239,68,68,0.1);color:#ef4444}h1{font-size:24px;margin:0 0 10px;font-weight:700}p{color:#94a3b8;margin:0 0 30px;line-height:1.5}.btn{display:inline-block;background:linear-gradient(to right,#8b5cf6,#6366f1);color:white;text-decoration:none;padding:12px 24px;border-radius:12px;font-weight:600;transition:all 0.3s ease}.btn:hover{box-shadow:0 0 20px rgba(139,92,246,0.5);transform:translateY(-2px)}svg{width:40px;height:40px}</style></head><body><div class='card %s'><div class='icon'>%s</div><h1>%s</h1><p>%s</p><a href='%s' class='btn'>%s</a></div></body></html>";

        String successIcon = "<svg fill='none' stroke='currentColor' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M5 13l4 4L19 7'></path></svg>";
        String errorIcon = "<svg fill='none' stroke='currentColor' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M6 18L18 6M6 6l12 12'></path></svg>";

        String loginUrl = "http://localhost:5173/login"; // Adjust based on frontend env if needed

        if (isActivated) {
            String html = String.format(htmlTemplate, "success", successIcon, "Account Activated!", "Your email has been successfully verified. You can now access your MONIFY account.", loginUrl, "Go to Login");
            return ResponseEntity.ok().header("Content-Type", "text/html").body(html);
        } else {
            String html = String.format(htmlTemplate, "error", errorIcon, "Activation Failed", "The activation link is invalid or has expired. Please try registering again.", loginUrl, "Back to Login");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Content-Type", "text/html").body(html);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthDto authDto) {
        try {
            if (!profileService.isAccountActive(authDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Account Not Activated"));
            }
            // Authenticating user
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my-profile")
    public ResponseEntity<ProfileDto> getPublicProfile() {
        ProfileDto profiledto = profileService.getPublicProfile(null);
        System.out.println(profiledto);
        return ResponseEntity.ok(profiledto);
    }

    @GetMapping("/remove-users")
    public ResponseEntity<String> removeUsers(@RequestParam String email) {
        profileService.deleteProfile(email);
        return ResponseEntity.ok("deleted user ");
    }
}
