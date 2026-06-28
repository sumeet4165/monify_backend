package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.dto.RecentTransactionDto;
import com.example.MONEYMANAGER.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/transactions/paged")
    public ResponseEntity<Page<RecentTransactionDto>> getPagedTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(dashboardService.getPagedTransactions(PageRequest.of(page, size)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getdashboard() {
        Map<String, Object> data = dashboardService.getDashboard();
        return ResponseEntity.ok(data);
    }
}
