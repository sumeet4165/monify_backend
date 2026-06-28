package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.dto.IncomeDto;
import com.example.MONEYMANAGER.service.IncomeService;
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
@RequestMapping("/incomes")
public class IncomeController {
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDto> Addincome(@Valid @RequestBody IncomeDto incomeDto) {
        IncomeDto saved = incomeService.AddIncome(incomeDto);
        return ResponseEntity.ok().body(saved);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<IncomeDto>> getPagedIncomes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(incomeService.getPagedIncomes(PageRequest.of(page, size, Sort.by("date").descending())));
    }

    @GetMapping
    public ResponseEntity<List<IncomeDto>> getIncomes() {
        List<IncomeDto> incomes = incomeService.getAllIncomeOfCurrentMonth();
        return ResponseEntity.ok(incomes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteincome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }
}
