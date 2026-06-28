package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.dto.*;
import com.example.MONEYMANAGER.service.AiService;
import com.example.MONEYMANAGER.service.ExpenseService;
import com.example.MONEYMANAGER.service.IncomeService;
import com.example.MONEYMANAGER.middleware.AiRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final AiRateLimiter aiRateLimiter;

    // Full AI Insights Dashboard
    @GetMapping("/insights")
    public ResponseEntity<?> getInsights(Principal principal) {
        if (!aiRateLimiter.allowRequest(principal.getName())) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Please wait before generating again."));
        }

        List<ExpenseDto> currentMonthExpenses = expenseService.getAllExpenseOfCurrentMonth();
        String totalExpense = expenseService.gettotalExpense().toString();
        List<IncomeDto> currentMonthIncomes = incomeService.getAllIncomeOfCurrentMonth();
        String totalIncome = incomeService.gettotalIncome().toString();

        AiInsightsDto insights = aiService.generateInsights(currentMonthExpenses, totalExpense, currentMonthIncomes, totalIncome);
        return ResponseEntity.ok(insights);
    }

    // NLP Quick Entry (auto-save to DB)
    @PostMapping("/quick-entry")
    public ResponseEntity<?> quickEntry(@RequestBody AiChatRequestDto request, Principal principal) {
        if (!aiRateLimiter.allowRequest(principal.getName())) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded."));
        }

        String text = request.getMessage();
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty."));
        }

        // AI parses text into structured transaction
        AiTransactionDto parsed = aiService.parseTransaction(text);
        if (parsed == null || parsed.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Could not parse transaction. Try something like: 'Spent 500 on groceries' or 'Got ₹2000 salary'"
            ));
        }

        // Auto-save to database based on type
        Object savedEntry;
        if ("income".equalsIgnoreCase(parsed.getType())) {
            savedEntry = incomeService.addIncomeFromAi(parsed);
        } else {
            savedEntry = expenseService.addExpenseFromAi(parsed);
        }

        return ResponseEntity.ok(Map.of(
                "parsed", parsed,
                "saved", savedEntry,
                "message", parsed.getType().equalsIgnoreCase("income")
                        ? "✅ Income of ₹" + parsed.getAmount() + " saved under " + parsed.getCategory()
                        : "✅ Expense of ₹" + parsed.getAmount() + " saved under " + parsed.getCategory()
        ));
    }

    // Structured AI Chat
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody AiChatRequestDto request, Principal principal) {
        if (!aiRateLimiter.allowRequest(principal.getName())) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded for AI chat."));
        }

        List<ExpenseDto> currentMonthExpenses = expenseService.getAllExpenseOfCurrentMonth();
        List<IncomeDto> currentMonthIncomes = incomeService.getAllIncomeOfCurrentMonth();

        AiChatResponseDto response = aiService.structuredChat(
                request.getMessage(), principal.getName(), currentMonthExpenses, currentMonthIncomes
        );
        return ResponseEntity.ok(response);
    }

    // Anomaly Detection (replaces /recurring)
    @GetMapping("/anomalies")
    public ResponseEntity<?> detectAnomalies(Principal principal) {
        List<ExpenseDto> currentMonth = expenseService.getAllExpenseOfCurrentMonth();

        // Get last month's data
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
        List<ExpenseDto> lastMonth = expenseService.getExpensesByDateRange(lastMonthStart, lastMonthEnd);

        List<String> anomalies = aiService.detectAnomalies(currentMonth, lastMonth);
        return ResponseEntity.ok(Map.of("anomalies", anomalies));
    }
}
