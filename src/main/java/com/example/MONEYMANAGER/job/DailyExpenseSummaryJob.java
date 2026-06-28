package com.example.MONEYMANAGER.job;

import com.example.MONEYMANAGER.dto.ExpenseDto;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.repository.ProfileRepository;
import com.example.MONEYMANAGER.service.EmailService;
import com.example.MONEYMANAGER.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyExpenseSummaryJob {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    public static final String JOB_NAME = "DAILY_EXPENSE_SUMMARY_JOB";

    @Scheduled(cron = "0 0 23 * * *", zone = "${money.manager.timezone:Asia/Kolkata}")
    @Transactional
    public void execute() {
        log.info("Starting Daily Expense Summary Job...");

        try {
            List<ProfileEntity> profiles = profileRepository.findAll();
            for (ProfileEntity profile : profiles) {
                if (!Boolean.TRUE.equals(profile.getIsactive())) continue;

                List<ExpenseDto> expenses = expenseService.getexpensesforuserondate(profile.getId(), LocalDate.now());

                if (!expenses.isEmpty()) {
                    double total = 0;
                    StringBuilder table = new StringBuilder();
                    table.append("<table style='width:100%;border-collapse:collapse;margin:25px 0;font-size:15px;text-align:left;border-radius:8px;overflow:hidden;box-shadow:0 0 20px rgba(0,0,0,0.02);'>");
                    table.append("<thead><tr style='background-color:#f4f4f5;color:#3f3f46;text-transform:uppercase;font-size:12px;letter-spacing:1px;'>");
                    table.append("<th style='padding:12px 15px;font-weight:600;'>#</th>");
                    table.append("<th style='padding:12px 15px;font-weight:600;'>Expense</th>");
                    table.append("<th style='padding:12px 15px;font-weight:600;'>Category</th>");
                    table.append("<th style='padding:12px 15px;font-weight:600;text-align:right;'>Amount</th>");
                    table.append("</tr></thead><tbody>");

                    int i = 1;
                    for (ExpenseDto expenseDto : expenses) {
                        table.append("<tr style='border-bottom:1px solid #e4e4e7;'>");
                        table.append("<td style='padding:12px 15px;color:#71717a;'>").append(i++).append("</td>");
                        table.append("<td style='padding:12px 15px;color:#27272a;font-weight:500;'>").append(expenseDto.getName()).append("</td>");
                        table.append("<td style='padding:12px 15px;'><span style='background-color:#e0e7ff;color:#4f46e5;padding:4px 8px;border-radius:12px;font-size:12px;font-weight:600;'>").append(expenseDto.getCategoryId() != null ? expenseDto.getCategoryname() : "N/A").append("</span></td>");
                        table.append("<td style='padding:12px 15px;text-align:right;color:#ef4444;font-weight:600;'>-₹").append(expenseDto.getAmount()).append("</td>");
                        table.append("</tr>");
                        total += expenseDto.getAmount() != null ? expenseDto.getAmount().doubleValue() : 0;
                    }

                    table.append("</tbody><tfoot><tr style='background-color:#fafafa;font-weight:bold;'>");
                    table.append("<td colspan='3' style='padding:12px 15px;text-align:right;color:#3f3f46;'>Total Spent:</td>");
                    table.append("<td style='padding:12px 15px;text-align:right;color:#ef4444;font-size:16px;'>-₹").append(String.format("%.2f", total)).append("</td>");
                    table.append("</tr></tfoot></table>");

                    String body = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <body style="margin:0;padding:20px;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;background-color:#f4f4f5;color:#18181b;-webkit-font-smoothing:antialiased;">
                        <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 25px -5px rgba(0,0,0,0.05);border:1px solid #e4e4e7;">
                            <div style="background:linear-gradient(135deg,#8b5cf6,#6366f1);padding:30px;text-align:center;">
                                <h1 style="color:#ffffff;margin:0;font-size:24px;font-weight:700;letter-spacing:-0.5px;">MONIFY</h1>
                            </div>
                            <div style="padding:40px 30px;">
                                <h2 style="margin:0 0 15px;font-size:20px;font-weight:600;color:#27272a;">Hi %s,</h2>
                                <p style="margin:0 0 10px;font-size:16px;line-height:1.6;color:#52525b;">Here is a summary of your recorded expenses for today.</p>
                                
                                %s
                                
                                <hr style="border:none;border-top:1px solid #e4e4e7;margin:30px 0;">
                                <p style="margin:0;font-size:14px;color:#a1a1aa;">Best regards,<br><strong style="color:#71717a;">The MONIFY Team</strong></p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """, profile.getFullname(), table.toString());

                    try {
                        emailService.sendEmail(profile.getEmail(), "Your Target Expense Summary - MONIFY", body);
                    } catch (Exception e) {
                        log.error("Failed to send daily expense summary to {}", profile.getEmail(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Daily Expense Summary Job failed", e);
        }
    }
}
