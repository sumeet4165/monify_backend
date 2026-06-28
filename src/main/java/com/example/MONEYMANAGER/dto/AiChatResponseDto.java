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
public class AiChatResponseDto {
    private String title;
    private List<PointEntry> points;
    private String insight;
    private String suggestion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PointEntry {
        private String category;
        private double amount;
    }
}
