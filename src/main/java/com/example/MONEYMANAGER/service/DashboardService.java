package com.example.MONEYMANAGER.service;

import com.example.MONEYMANAGER.dto.ExpenseDto;
import com.example.MONEYMANAGER.dto.IncomeDto;
import com.example.MONEYMANAGER.dto.RecentTransactionDto;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboard() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> map = new LinkedHashMap<>();

        List<IncomeDto> latestIncomes = incomeService.getLatest5IncomesOfCurrentUser();
        List<ExpenseDto> latestExpenses = expenseService.getLatest5ExpensesOfCurrentUser();

        List<RecentTransactionDto> transactions = Stream.concat(
                latestIncomes.stream().map(income ->
                        RecentTransactionDto.builder()
                                .id(income.getId())
                                .profileId(profile.getId())
                                .icon(income.getIcon())
                                .name(income.getName())
                                .amount(income.getAmount())
                                .date(income.getDate())
                                .createdAt(income.getCreatedat())
                                .updatedAt(income.getUpdatedat())
                                .type("income")
                                .build()
                ),
                latestExpenses.stream().map(expense ->
                        RecentTransactionDto.builder()
                                .id(expense.getId())
                                .profileId(profile.getId())
                                .icon(expense.getIcon())
                                .name(expense.getName())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .createdAt(expense.getCreatedat())
                                .updatedAt(expense.getUpdatedat())
                                .type("expense")
                                .build()
                )
        )
        .sorted(
                Comparator.comparing(RecentTransactionDto::getDate, Comparator.reverseOrder())
                        .thenComparing(RecentTransactionDto::getCreatedAt, Comparator.reverseOrder())
        )
        .collect(Collectors.toList());

        map.put("totalBalance", incomeService.gettotalIncome().subtract(expenseService.gettotalExpense()));
        map.put("totalIncome", incomeService.gettotalIncome());
        map.put("totalExpenses", expenseService.gettotalExpense());
        map.put("recent5Expenses", latestExpenses);
        map.put("recent5Income", latestIncomes);
        map.put("recentTransactions", transactions);

        return map;
    }

    public List<RecentTransactionDto> getRecenetTransactionOfCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeDto> allIncomes = incomeService.getAllIncomeOfCurrentMonth();
        List<ExpenseDto> allExpenses = expenseService.getAllExpenseOfCurrentMonth();

        return Stream.concat(
                allIncomes.stream().map(income ->
                        RecentTransactionDto.builder()
                                .id(income.getId()).profileId(profile.getId())
                                .icon(income.getIcon()).name(income.getName())
                                .amount(income.getAmount()).date(income.getDate())
                                .createdAt(income.getCreatedat()).updatedAt(income.getUpdatedat())
                                .type("income").build()),
                allExpenses.stream().map(expense ->
                        RecentTransactionDto.builder()
                                .id(expense.getId()).profileId(profile.getId())
                                .icon(expense.getIcon()).name(expense.getName())
                                .amount(expense.getAmount()).date(expense.getDate())
                                .createdAt(expense.getCreatedat()).updatedAt(expense.getUpdatedat())
                                .type("expense").build())
        )
        .sorted(Comparator.comparing(RecentTransactionDto::getDate, Comparator.reverseOrder())
                .thenComparing(RecentTransactionDto::getCreatedAt, Comparator.reverseOrder()))
        .collect(Collectors.toList());
    }

    public Page<RecentTransactionDto> getPagedTransactions(Pageable pageable) {
        List<RecentTransactionDto> all = getRecenetTransactionOfCurrentUser();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<RecentTransactionDto> subList = start > all.size() ? new ArrayList<>() : all.subList(start, end);
        return new PageImpl<>(subList, pageable, all.size());
    }
}
