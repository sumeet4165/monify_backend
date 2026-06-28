package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.service.EmailService;
import com.example.MONEYMANAGER.service.ExcelService;
import com.example.MONEYMANAGER.service.ExpenseService;
import com.example.MONEYMANAGER.service.IncomeService;
import com.example.MONEYMANAGER.service.ProfileService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {
    private final ExcelService excelService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final EmailService emailService;
    private final ProfileService profileService;

    @GetMapping("/income-excel")
    public ResponseEntity<Void> emailIncomeExcel() throws IOException, MessagingException {
        ProfileEntity profile = profileService.getCurrentProfile();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelService.writeIncomesToExcel(outputStream, incomeService.getAllIncomeOfCurrentMonth());
        String body = """
        <html><body style='font-family: Arial, sans-serif; background-color: #0B0F19; color: #e4e4e7; padding: 20px; text-align: center;'>
            <div style='max-width: 600px; margin: 0 auto; background: #18181b; padding: 30px; border-radius: 12px; border: 1px solid #27272a;'>
                <h2 style='color: #a78bfa;'>Your Income Report is Ready ✨</h2>
                <p style='color: #a1a1aa; font-size: 16px; line-height: 1.6;'>
                    Hi %s, attached is your requested income details Excel file for this month.
                </p>
                <div style='margin-top: 30px; padding: 20px; background: rgba(167, 139, 250, 0.1); border-left: 4px solid #a78bfa; border-radius: 6px; text-align: left;'>
                    <p style='margin: 0; color: #e4e4e7;'>💡 <strong>Tip:</strong> Log into your Dashboard to view advanced MoneyManager AI analytics.</p>
                </div>
                <p style='margin-top: 40px; color: #71717a; font-size: 13px;'>© 2026 MoneyManager AI</p>
            </div>
        </body></html>
        """.formatted(profile.getFullname() != null ? profile.getFullname() : "there");

        emailService.sendemailwithattachment(
                profile.getEmail(),
                "Your Monthly Income Report by MoneyManager AI",
                body,
                outputStream.toByteArray(),
                "Income.xlsx"
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/expense-excel")
    public ResponseEntity<Void> emailExpenseExcel() throws IOException, MessagingException {
        ProfileEntity profile = profileService.getCurrentProfile();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelService.writeExpensesToExcel(outputStream, expenseService.getAllExpenseOfCurrentMonth());
        String body = """
        <html><body style='font-family: Arial, sans-serif; background-color: #0B0F19; color: #e4e4e7; padding: 20px; text-align: center;'>
            <div style='max-width: 600px; margin: 0 auto; background: #18181b; padding: 30px; border-radius: 12px; border: 1px solid #27272a;'>
                <h2 style='color: #f87171;'>Your Expense Report is Ready 📊</h2>
                <p style='color: #a1a1aa; font-size: 16px; line-height: 1.6;'>
                    Hi %s, attached is your requested expense details Excel file for this month.
                </p>
                <div style='margin-top: 30px; padding: 20px; background: rgba(248, 113, 113, 0.1); border-left: 4px solid #f87171; border-radius: 6px; text-align: left;'>
                    <p style='margin: 0; color: #e4e4e7;'>💡 <strong>Anomaly Check:</strong> Upload your fresh data to our AI engine to check for any spending anomalies.</p>
                </div>
                <p style='margin-top: 40px; color: #71717a; font-size: 13px;'>© 2026 MoneyManager AI</p>
            </div>
        </body></html>
        """.formatted(profile.getFullname() != null ? profile.getFullname() : "there");

        emailService.sendemailwithattachment(
                profile.getEmail(),
                "Your Monthly Expense Report by MoneyManager AI",
                body,
                outputStream.toByteArray(),
                "Expense.xlsx"
        );
        return ResponseEntity.ok().build();
    }
}
