package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.dto.ExpenseDto;
import com.example.MONEYMANAGER.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto> AddExpense(@Valid @RequestBody ExpenseDto expenseDto) {
        ExpenseDto saved = expenseService.AddExpense(expenseDto);
        return ResponseEntity.ok().body(saved);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<ExpenseDto>> getPagedExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(expenseService.getPagedExpenses(PageRequest.of(page, size, Sort.by("date").descending())));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpenses() {
        List<ExpenseDto> expenses = expenseService.getAllExpenseOfCurrentMonth();
        return ResponseEntity.ok(expenses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteexpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
