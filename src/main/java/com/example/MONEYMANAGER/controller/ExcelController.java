package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.service.ExcelService;
import com.example.MONEYMANAGER.service.ExpenseService;
import com.example.MONEYMANAGER.service.IncomeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @GetMapping("/download/incomes")
    public void downloadIncomesExcel(
            HttpServletResponse response,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=incomes.xlsx");

        excelService.writeIncomesToExcel(
                response.getOutputStream(),
                startDate != null && endDate != null ? incomeService.getIncomesByDateRange(startDate, endDate) : incomeService.getAllIncomeOfCurrentMonth()
        );
    }

    @GetMapping("/download/expenses")
    public void downloadExpensesExcel(
            HttpServletResponse response,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");

        excelService.writeExpensesToExcel(
                response.getOutputStream(),
                startDate != null && endDate != null ? expenseService.getExpensesByDateRange(startDate, endDate) : expenseService.getAllExpenseOfCurrentMonth()
        );
    }
}
