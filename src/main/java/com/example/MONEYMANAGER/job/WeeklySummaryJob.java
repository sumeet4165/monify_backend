package com.example.MONEYMANAGER.job;

import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.repository.ProfileRepository;
import com.example.MONEYMANAGER.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklySummaryJob {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    public static final String JOB_NAME = "WEEKLY_SUMMARY_JOB";

    // Send email every Sunday at 9 AM (timezone configured via property)
    @Scheduled(cron = "0 0 9 * * SUN", zone = "${money.manager.timezone:Asia/Kolkata}")
    public void execute() {
        log.info("Starting Weekly Summary Job...");
        
        try {
            List<ProfileEntity> allUsers = profileRepository.findAll();
            for (ProfileEntity user : allUsers) {
                if (!Boolean.TRUE.equals(user.getIsactive())) continue;
                
                try {
                    String subject = "Your MONIFY Weekly Summary";
                    String text = """
                    <!DOCTYPE html>
                    <html>
                    <body style="margin:0;padding:20px;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;background-color:#f4f4f5;color:#18181b;">
                        <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e4e4e7;">
                            <div style="background:linear-gradient(135deg,#8b5cf6,#6366f1);padding:30px;text-align:center;">
                                <h1 style="color:#ffffff;margin:0;font-size:24px;font-weight:700;">MONIFY</h1>
                            </div>
                            <div style="padding:40px 30px;">
                                <h2 style="margin:0 0 15px;font-size:20px;font-weight:600;color:#27272a;">Hi %s,</h2>
                                <p style="margin:0 0 25px;font-size:16px;line-height:1.6;color:#52525b;">Stay on top of your finances with MONIFY. Log in to check your latest AI insights and health score.</p>
                                <hr style="border:none;border-top:1px solid #e4e4e7;margin:30px 0;">
                                <p style="margin:0;font-size:14px;color:#a1a1aa;">Best regards,<br><strong style="color:#71717a;">The MONIFY Team</strong></p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(user.getFullname());

                    emailService.sendEmail(user.getEmail(), subject, text);
                } catch (Exception e) {
                    log.error("Failed to send weekly summary to {}", user.getEmail(), e);
                }
            }
        } catch (Exception e) {
            log.error("Weekly Summary Job failed", e);
        }
    }
}
