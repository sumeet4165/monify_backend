package com.example.MONEYMANAGER.service;

import com.example.MONEYMANAGER.dto.ExpenseDto;
import com.example.MONEYMANAGER.dto.IncomeDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ExcelService {

    // Write Incomes to Excel
    public void writeIncomesToExcel(OutputStream out, List<IncomeDto> incomes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Incomes");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("S No");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Date");
            header.createCell(4).setCellValue("Amount");

            // Fill rows
            IntStream.range(0, incomes.size()).forEach(i -> {
                IncomeDto income = incomes.get(i);
                Row row = sheet.createRow(i + 1);

                String name = income.getName() != null ? income.getName() : "N/A";
                String category = income.getCategoryname() != null ? income.getCategoryname() : "N/A";
                String date = income.getDate() != null ? income.getDate().toString() : "N/A";
                String amount = income.getAmount() != null ? income.getAmount().toString() : "0.0";

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(name);
                row.createCell(2).setCellValue(category);
                row.createCell(3).setCellValue(date);
                row.createCell(4).setCellValue(amount);
            });

            workbook.write(out);
        }
    }

    // Write Expenses to Excel
    public void writeExpensesToExcel(OutputStream out, List<ExpenseDto> expenses) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("S No");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Date");
            header.createCell(4).setCellValue("Amount");

            // Fill rows
            IntStream.range(0, expenses.size()).forEach(i -> {
                ExpenseDto expense = expenses.get(i);
                Row row = sheet.createRow(i + 1);

                String name = expense.getName() != null ? expense.getName() : "N/A";
                String category = expense.getCategoryname() != null ? expense.getCategoryname() : "N/A";
                String date = expense.getDate() != null ? expense.getDate().toString() : "N/A";
                String amount = expense.getAmount() != null ? expense.getAmount().toString() : "0.0";

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(name);
                row.createCell(2).setCellValue(category);
                row.createCell(3).setCellValue(date);
                row.createCell(4).setCellValue(amount);
            });

            workbook.write(out);
        }
    }
}
