package com.example.MONEYMANAGER.job;

import com.example.MONEYMANAGER.dto.ExpenseDto;
import com.example.MONEYMANAGER.dto.IncomeDto;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.repository.ProfileRepository;
import com.example.MONEYMANAGER.service.EmailService;
import com.example.MONEYMANAGER.service.ExpenseService;
import com.example.MONEYMANAGER.service.IncomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlySummaryJob {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public static final String JOB_NAME = "MONTHLY_SUMMARY_JOB";

    // Executes on the 1st of every month at 9:00 AM
    @Scheduled(cron = "0 0 9 1 * *", zone = "${money.manager.timezone:Asia/Kolkata}")
    public void execute() {
        log.info("Starting Monthly Summary Job...");
        LocalDate lastMonthDate = LocalDate.now().minusMonths(1);
        String lastMonthName = lastMonthDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int lastMonthYear = lastMonthDate.getYear();

        LocalDate startDate = lastMonthDate.withDayOfMonth(1);
        LocalDate endDate = lastMonthDate.withDayOfMonth(lastMonthDate.lengthOfMonth());

        try {
            List<ProfileEntity> profiles = profileRepository.findAll();
            for (ProfileEntity profile : profiles) {
                if (!Boolean.TRUE.equals(profile.getIsactive())) continue;

                // Query last month's data
                List<ExpenseDto> expenses = expenseService.getExpensesByDateRange(startDate, endDate);
                List<IncomeDto> incomes = incomeService.getIncomesByDateRange(startDate, endDate);

                double totalExpenses = expenses.stream().mapToDouble(e -> e.getAmount().doubleValue()).sum();
                double totalIncome = incomes.stream().mapToDouble(i -> i.getAmount().doubleValue()).sum();
                double savings = totalIncome - totalExpenses;
                double savingsRate = totalIncome > 0 ? (savings / totalIncome) * 100 : 0;

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
                            <p style="margin:0 0 25px;font-size:16px;line-height:1.6;color:#52525b;">Here is your financial report card for <strong>%s %d</strong>.</p>
                            
                            <div style="grid-template-columns: 1fr 1fr; gap: 15px; margin: 25px 0;">
                                <div style="background-color: #f4f4f5; padding: 15px; border-radius: 10px; margin-bottom:10px;">
                                    <span style="font-size: 13px; color: #71717a; text-transform: uppercase; font-weight: 600;">Total Income</span>
                                    <h3 style="margin: 5px 0 0; color: #10b981; font-size: 22px;">₹%.2f</h3>
                                </div>
                                <div style="background-color: #f4f4f5; padding: 15px; border-radius: 10px; margin-bottom:10px;">
                                    <span style="font-size: 13px; color: #71717a; text-transform: uppercase; font-weight: 600;">Total Expenses</span>
                                    <h3 style="margin: 5px 0 0; color: #ef4444; font-size: 22px;">₹%.2f</h3>
                                </div>
                                <div style="background-color: #f4f4f5; padding: 15px; border-radius: 10px; margin-bottom:10px;">
                                    <span style="font-size: 13px; color: #71717a; text-transform: uppercase; font-weight: 600;">Net Savings</span>
                                    <h3 style="margin: 5px 0 0; color: #4f46e5; font-size: 22px;">₹%.2f</h3>
                                </div>
                                <div style="background-color: #f4f4f5; padding: 15px; border-radius: 10px;">
                                    <span style="font-size: 13px; color: #71717a; text-transform: uppercase; font-weight: 600;">Savings Rate</span>
                                    <h3 style="margin: 5px 0 0; color: #8b5cf6; font-size: 22px;">%.1f%%</h3>
                                </div>
                            </div>
                            
                            <hr style="border:none;border-top:1px solid #e4e4e7;margin:30px 0;">
                            <p style="margin:0;font-size:14px;color:#a1a1aa;">Best regards,<br><strong style="color:#71717a;">The MONIFY Team</strong></p>
                        </div>
                    </div>
                </body>
                </html>
                """, profile.getFullname(), lastMonthName, lastMonthYear, totalIncome, totalExpenses, savings, savingsRate);

                try {
                    emailService.sendEmail(profile.getEmail(), "Your Monthly Financial Report card - MONIFY", body);
                } catch (Exception e) {
                    log.error("Failed to send monthly summary to {}", profile.getEmail(), e);
                }
            }
        } catch (Exception e) {
            log.error("Monthly Summary Job failed", e);
        }
    }
}
