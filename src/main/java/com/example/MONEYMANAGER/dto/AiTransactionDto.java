package com.example.MONEYMANAGER.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTransactionDto {
    private String type;        // "expense" or "income"
    private double amount;
    private String category;
    private String description;
    private String date;        // yyyy-MM-dd
}
