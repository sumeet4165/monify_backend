package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.dto.ExpenseDto;
import com.example.MONEYMANAGER.dto.FilterDto;
import com.example.MONEYMANAGER.dto.IncomeDto;
import com.example.MONEYMANAGER.service.ExpenseService;
import com.example.MONEYMANAGER.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<?> filtertransactions(@RequestBody FilterDto filterDto) {
        LocalDate startDate = filterDto.getStartDate() != null ? filterDto.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filterDto.getEndDate() != null ? filterDto.getEndDate() : LocalDate.now();
        String keyword = filterDto.getKeyword() != null ? filterDto.getKeyword() : "";
        String sortField = filterDto.getSortField() != null ? filterDto.getSortField() : "date";
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(filterDto.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortField);

        if ("income".equalsIgnoreCase(filterDto.getType())) {
            List<IncomeDto> incomes = incomeService.filterincomes(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(incomes);
        } else if ("expense".equalsIgnoreCase(filterDto.getType())) {
            List<ExpenseDto> expenses = expenseService.filterexpenses(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(expenses);
        } else {
            return ResponseEntity.badRequest().body("INVALID TYPE INCOME / EXPENSE ONLY");
        }
    }
}
