package com.example.MONEYMANAGER.job;

import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.repository.ProfileRepository;
import com.example.MONEYMANAGER.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReminderJob {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    @Value("${money.manager.frontend.url:localhost:5173}")
    private String frontendUrl;

    public static final String JOB_NAME = "DAILY_REMINDER_JOB";

    @Scheduled(cron = "0 0 22 * * *", zone = "${money.manager.timezone:Asia/Kolkata}")
    public void execute() {
        log.info("Starting Daily Reminder Job...");

        try {
            List<ProfileEntity> profiles = profileRepository.findAll();
            for (ProfileEntity profile : profiles) {
                if (!Boolean.TRUE.equals(profile.getIsactive())) continue;

                String body = """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:20px;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;background-color:#f4f4f5;color:#18181b;-webkit-font-smoothing:antialiased;">
                    <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 25px -5px rgba(0,0,0,0.05);border:1px solid #e4e4e7;">
                        <div style="background:linear-gradient(135deg,#8b5cf6,#6366f1);padding:30px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:24px;font-weight:700;letter-spacing:-0.5px;">MONIFY</h1>
                        </div>
                        <div style="padding:40px 30px;">
                            <h2 style="margin:0 0 15px;font-size:20px;font-weight:600;color:#27272a;">Hi %s,</h2>
                            <p style="margin:0 0 25px;font-size:16px;line-height:1.6;color:#52525b;">This is a friendly reminder to <strong>add your income and expenses</strong> for today. Keeping your transactions up to date ensures your AI insights and health score remain accurate.</p>
                            
                            <div style="text-align:center;margin:35px 0;">
                                <a href="http://%s" style="display:inline-block;padding:14px 28px;background:linear-gradient(135deg,#8b5cf6,#6366f1);color:#ffffff;text-decoration:none;border-radius:12px;font-weight:600;font-size:16px;letter-spacing:0.3px;">Go to Dashboard</a>
                            </div>
                            
                            <hr style="border:none;border-top:1px solid #e4e4e7;margin:30px 0;">
                            <p style="margin:0;font-size:14px;color:#a1a1aa;">Best regards,<br><strong style="color:#71717a;">The MONIFY Team</strong></p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(profile.getFullname(), frontendUrl);

                try {
                    emailService.sendEmail(profile.getEmail(), "Reminder: Update Your MONIFY Dashboard", body);
                } catch (Exception e) {
                    log.error("Failed to send daily reminder to {}", profile.getEmail(), e);
                }
            }
        } catch (Exception e) {
            log.error("Daily Reminder Job failed", e);
        }
    }
}
