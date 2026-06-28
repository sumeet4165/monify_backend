package com.example.MONEYMANAGER.service;

import com.example.MONEYMANAGER.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    // Allowed categories
    private static final List<String> VALID_CATEGORIES = List.of(
            "Food", "Transport", "Shopping", "Bills", "Entertainment",
            "Salary", "Freelance", "Investment", "Gift", "Rent",
            "Medical", "Education", "Travel", "Subscription", "Other"
    );

    // ─────────────────────────────────────────────
    //  1) NLP Transaction Parser (income + expense)
    // ─────────────────────────────────────────────
    public AiTransactionDto parseTransaction(String userText) {
        try {
            BeanOutputConverter<AiTransactionDto> converter = new BeanOutputConverter<>(AiTransactionDto.class);

            String systemPrompt = "You are a precise financial transaction parser. " +
                    "The user will describe a financial transaction in natural language. " +
                    "You MUST determine if it is an EXPENSE or INCOME. " +
                    "Rules:\n" +
                    "- Words like 'spent', 'bought', 'paid', 'purchased', 'cost' → type = \"expense\"\n" +
                    "- Words like 'received', 'got', 'earned', 'salary', 'credited', 'refund' → type = \"income\"\n" +
                    "- Extract the numeric amount. If currency symbol like ₹ or $ is present, ignore it, just extract the number.\n" +
                    "- Pick ONE category from this list ONLY: " + String.join(", ", VALID_CATEGORIES) + "\n" +
                    "- If unsure, use \"Other\"\n" +
                    "- For date: if the user says 'today', use today's date. If 'yesterday', use yesterday. Otherwise default to today.\n" +
                    "- Today's date is: " + java.time.LocalDate.now() + "\n\n" +
                    "Return ONLY valid JSON matching this schema: " + converter.getFormat() + "\n" +
                    "Do NOT wrap in markdown. Output pure raw JSON only.";

            String rawResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userText)
                    .call()
                    .content();

            // Strip markdown code fences if present
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }

            AiTransactionDto result = converter.convert(cleaned);

            // Validate & sanitize
            if (result != null) {
                if (result.getType() == null || (!result.getType().equalsIgnoreCase("expense") && !result.getType().equalsIgnoreCase("income"))) {
                    result.setType("expense");
                }
                result.setType(result.getType().toLowerCase());
                result.setCategory(validateCategory(result.getCategory()));
                if (result.getDate() == null || result.getDate().isBlank()) {
                    result.setDate(java.time.LocalDate.now().toString());
                }
                if (result.getAmount() <= 0) {
                    result.setAmount(0);
                }
            }
            return result;

        } catch (Exception e) {
            System.err.println("AI Transaction Parse Failed: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────
    //  2) Strict Category Classifier (Auto Categorization)
    // ─────────────────────────────────────────────
    public String categorizeExpense(String title, String note, List<String> existingCategories) {
        try {
            String categoriesStr = String.join(", ", VALID_CATEGORIES);

            String systemPrompt = "You are a financial transaction categorizer. " +
                    "You will receive a transaction description. " +
                    "You MUST reply with EXACTLY ONE category from this list: " + categoriesStr + "\n" +
                    "Rules:\n" +
                    "- Reply with ONLY the category name, nothing else\n" +
                    "- No explanation, no punctuation, no extra words\n" +
                    "- If it doesn't fit any category, reply with: Other\n" +
                    "- The reply must be a single word or two-word category name";

            String userPrompt = "Transaction: " + (title != null ? title : "") +
                    (note != null && !note.isBlank() ? " | Note: " + note : "");

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            return validateCategory(response.trim());
        } catch (Exception e) {
            System.err.println("AI Categorization Failed: " + e.getMessage());
            return "Other";
        }
    }

    // ─────────────────────────────────────────────
    //  4) Full AI Insights Generation
    // ─────────────────────────────────────────────
    public AiInsightsDto generateInsights(List<ExpenseDto> expenses, String totalExpense,
                                          List<IncomeDto> incomes, String totalIncome) {
        try {
            String expensesSummary = expenses.stream()
                    .map(e -> String.format("- %s: ₹%s [%s] on %s",
                            e.getName() != null ? e.getName() : "Unnamed",
                            e.getAmount(), e.getCategoryname(), e.getDate()))
                    .collect(Collectors.joining("\n"));

            String incomesSummary = incomes.stream()
                    .map(i -> String.format("- %s: ₹%s [%s] on %s",
                            i.getName() != null ? i.getName() : "Unnamed",
                            i.getAmount(), i.getCategoryname(), i.getDate()))
                    .collect(Collectors.joining("\n"));

            double totalExp = parseNumber(totalExpense);
            double totalInc = parseNumber(totalIncome);
            int healthScore = calculateHealthScore(totalExp, totalInc, expenses.size());

            // Build category breakdown
            Map<String, Double> catMap = new LinkedHashMap<>();
            for (ExpenseDto e : expenses) {
                String cat = e.getCategoryname() != null ? e.getCategoryname() : "Other";
                catMap.merge(cat, e.getAmount().doubleValue(), Double::sum);
            }
            List<AiInsightsDto.CategoryBreakdown> breakdownList = new ArrayList<>();
            for (Map.Entry<String, Double> entry : catMap.entrySet()) {
                breakdownList.add(AiInsightsDto.CategoryBreakdown.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .percentage(totalExp > 0 ? Math.round(entry.getValue() / totalExp * 100.0) : 0)
                        .build());
            }
            breakdownList.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

            BeanOutputConverter<AiInsightsDto> converter = new BeanOutputConverter<>(AiInsightsDto.class);

            String systemPrompt = "You are an elite AI financial advisor for MONIFY app. " +
                    "Analyze the user's financial data and return a comprehensive report. " +
                    "Health Score (pre-calculated): " + healthScore + "/100\n" +
                    "Return STRICT JSON matching: " + converter.getFormat() + "\n" +
                    "Fill these fields:\n" +
                    "- summary: 2-3 sentence executive overview\n" +
                    "- income: { total, sources (list category names) }\n" +
                    "- expenses: { total, topCategories (top 3 category names) }\n" +
                    "- insight: one key financial insight\n" +
                    "- recommendation: primary actionable advice\n" +
                    "- healthScore: " + healthScore + "\n" +
                    "- budgetPrediction: predict next month spending in one sentence\n" +
                    "- savingsPotential: how much could be saved, one sentence\n" +
                    "- anomalyAlerts: list unusual spending patterns (empty list if none)\n" +
                    "- recommendations: list of 3-4 actionable tips\n" +
                    "- breakdown: leave as null (we compute it server-side)\n" +
                    "- rawResponse: leave as null\n\n" +
                    "Do NOT wrap in markdown. Output pure raw JSON only. No ```json fences.";

            String userPrompt = String.format(
                    "Total Income: ₹%s\nTotal Expenses: ₹%s\n\nIncomes:\n%s\n\nExpenses:\n%s",
                    totalIncome, totalExpense, incomesSummary, expensesSummary);

            String rawResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }

            AiInsightsDto insights = converter.convert(cleaned);
            if (insights != null) {
                insights.setRawResponse(rawResponse);
                insights.setHealthScore(healthScore);
                insights.setBreakdown(breakdownList);
            }
            return insights;

        } catch (Exception e) {
            System.err.println("AI Insights Generation Failed: " + e.getMessage());
            return AiInsightsDto.builder()
                    .summary("Unable to generate AI insights at this time: " + e.getMessage())
                    .healthScore(0)
                    .anomalyAlerts(List.of())
                    .recommendations(List.of("Try again later"))
                    .breakdown(List.of())
                    .rawResponse("")
                    .build();
        }
    }

    // ─────────────────────────────────────────────
    //  5) Anomaly Detection
    // ─────────────────────────────────────────────
    public List<String> detectAnomalies(List<ExpenseDto> currentMonth, List<ExpenseDto> lastMonth) {
        List<String> anomalies = new ArrayList<>();

        // Build category totals for current and last month
        Map<String, Double> currentCats = buildCategoryTotals(currentMonth);
        Map<String, Double> lastCats = buildCategoryTotals(lastMonth);

        double currentTotal = currentMonth.stream()
                .mapToDouble(e -> e.getAmount().doubleValue()).sum();
        double lastTotal = lastMonth.stream()
                .mapToDouble(e -> e.getAmount().doubleValue()).sum();

        // Check overall spending spike
        if (lastTotal > 0 && currentTotal > lastTotal * 1.5) {
            int pct = (int) ((currentTotal - lastTotal) / lastTotal * 100);
            anomalies.add("⚠️ Overall spending increased by " + pct + "% compared to last month (₹" +
                    String.format("%.0f", lastTotal) + " → ₹" + String.format("%.0f", currentTotal) + ")");
        }

        // Check per-category spikes
        for (Map.Entry<String, Double> entry : currentCats.entrySet()) {
            String cat = entry.getKey();
            double current = entry.getValue();
            double last = lastCats.getOrDefault(cat, 0.0);

            if (last > 0 && current > last * 2) {
                int pct = (int) ((current - last) / last * 100);
                anomalies.add("📈 " + cat + " spending spiked by " + pct + "% (₹" +
                        String.format("%.0f", last) + " → ₹" + String.format("%.0f", current) + ")");
            } else if (last == 0 && current > 1000) {
                anomalies.add("🆕 New high-spend category: " + cat + " at ₹" + String.format("%.0f", current));
            }
        }

        // Check for unusually large single transactions
        double avg = currentMonth.isEmpty() ? 0 :
                currentTotal / currentMonth.size();
        for (ExpenseDto e : currentMonth) {
            if (avg > 0 && e.getAmount().doubleValue() > avg * 5 && e.getAmount().doubleValue() > 500) {
                anomalies.add("🔺 Unusually large transaction: " + e.getName() +
                        " — ₹" + e.getAmount() + " (" + e.getCategoryname() + ")");
            }
        }

        if (anomalies.isEmpty()) {
            anomalies.add("✅ No unusual spending patterns detected. Your finances look stable.");
        }

        return anomalies;
    }

    // ─────────────────────────────────────────────
    //  6) Structured Chat Response
    // ─────────────────────────────────────────────
    public AiChatResponseDto structuredChat(String message, String userId,
                                            List<ExpenseDto> expenses, List<IncomeDto> incomes) {
        try {
            String expSummary = expenses.stream()
                    .map(e -> String.format("- %s: ₹%s (%s)", e.getCategoryname(), e.getAmount(), e.getDate()))
                    .collect(Collectors.joining("\n"));
            String incSummary = incomes.stream()
                    .map(i -> String.format("- %s: ₹%s (%s)", i.getCategoryname(), i.getAmount(), i.getDate()))
                    .collect(Collectors.joining("\n"));

            BeanOutputConverter<AiChatResponseDto> converter = new BeanOutputConverter<>(AiChatResponseDto.class);

            String systemPrompt = "You are MONIFY AI, a premium financial assistant. " +
                    "The user is asking a financial question. " +
                    "You MUST return a structured JSON response matching: " + converter.getFormat() + "\n\n" +
                    "Rules:\n" +
                    "- title: a short title for your answer (max 8 words)\n" +
                    "- points: list of relevant data points with category and amount (can be empty if not applicable)\n" +
                    "- insight: your key financial observation (1-2 sentences)\n" +
                    "- suggestion: actionable advice (1-2 sentences)\n\n" +
                    "Do NOT use markdown. Do NOT wrap in code fences. Pure JSON only.\n\n" +
                    "User's Expense Context:\n" + expSummary + "\n\nUser's Income Context:\n" + incSummary;

            String rawResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(message)
                    .call()
                    .content();

            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }

            AiChatResponseDto result = converter.convert(cleaned);
            return result != null ? result : AiChatResponseDto.builder()
                    .title("Response")
                    .insight(rawResponse)
                    .suggestion("Please try asking in a different way.")
                    .points(List.of())
                    .build();

        } catch (Exception e) {
            System.err.println("Structured Chat Failed: " + e.getMessage());
            return AiChatResponseDto.builder()
                    .title("Error")
                    .insight("I encountered a problem analyzing your request.")
                    .suggestion("Please try again later.")
                    .points(List.of())
                    .build();
        }
    }

    private String validateCategory(String raw) {
        if (raw == null || raw.isBlank()) return "Other";
        String cleaned = raw.trim().replaceAll("[^a-zA-Z ]", "");
        for (String valid : VALID_CATEGORIES) {
            if (valid.equalsIgnoreCase(cleaned)) return valid;
        }
        for (String valid : VALID_CATEGORIES) {
            if (cleaned.toLowerCase().contains(valid.toLowerCase())) return valid;
        }
        return "Other";
    }

    private Map<String, Double> buildCategoryTotals(List<ExpenseDto> expenses) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (ExpenseDto e : expenses) {
            String cat = e.getCategoryname() != null ? e.getCategoryname() : "Other";
            map.merge(cat, e.getAmount().doubleValue(), Double::sum);
        }
        return map;
    }

    private int calculateHealthScore(double totalExp, double totalInc, int txnCount) {
        int baseScore = 50;
        if (totalInc > 0) {
            double ratio = totalExp / totalInc;
            if (ratio <= 0.5) baseScore = 95;
            else if (ratio <= 0.75) baseScore = 75;
            else if (ratio <= 0.9) baseScore = 60;
            else if (ratio <= 1.0) baseScore = 40;
            else baseScore = 20;
        } else {
            baseScore = (totalExp > 0) ? 10 : 50;
        }
        if (txnCount > 50) baseScore -= 5;
        return Math.max(10, Math.min(100, baseScore));
    }

    private double parseNumber(String str) {
        try {
            return Double.parseDouble(str.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
