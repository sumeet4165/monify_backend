package com.example.MONEYMANAGER.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsightsDto {

    private String summary;
    private IncomeData income;
    private ExpenseData expenses;
    private String insight;
    private String recommendation;
    private String rawResponse;

    // New fields for enhanced dashboard
    private int healthScore;
    private String budgetPrediction;
    private String savingsPotential;
    private List<CategoryBreakdown> breakdown;
    private List<String> anomalyAlerts;
    private List<String> recommendations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncomeData {
        private double total;
        private List<String> sources;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseData {
        private double total;
        private List<String> topCategories;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryBreakdown {
        private String category;
        private double amount;
        private double percentage;
    }
}
