package com.example.MONEYMANAGER.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncomeDto {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    private String icon;
    private String categoryname;
    private Long CategoryId;
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative")
    private BigDecimal amount;
    @NotNull(message = "Date is required")
    private LocalDate date;
    private LocalDateTime createdat;
    private LocalDateTime updatedat;
}
