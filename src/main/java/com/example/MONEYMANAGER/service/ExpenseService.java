package com.example.MONEYMANAGER.service;

import com.example.MONEYMANAGER.dto.ExpenseDto;
import com.example.MONEYMANAGER.dto.AiTransactionDto;
import com.example.MONEYMANAGER.entity.CategoryEntity;
import com.example.MONEYMANAGER.entity.ExpenseEntity;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.mapper.ExpenseMapper;
import com.example.MONEYMANAGER.repository.CategoryRepository;
import com.example.MONEYMANAGER.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;
    private final ExpenseMapper expenseMapper;
    private final AiService aiService;

    // Adding expenses to DB
    public ExpenseDto AddExpense(ExpenseDto expenseDto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        // AI categorization
        if (expenseDto.getCategoryId() == null || "AUTO".equalsIgnoreCase(expenseDto.getCategoryname())) {
            List<CategoryEntity> existingCats = categoryRepository.findByProfileId(profile.getId());
            List<String> catNames = existingCats.stream().map(CategoryEntity::getCategoryName).collect(Collectors.toList());

            String predictedCatName = aiService.categorizeExpense(expenseDto.getName(), "", catNames);

            // Find or create this category
            CategoryEntity category = categoryRepository.findByProfileId(profile.getId()).stream()
                    .filter(c -> c.getCategoryName().equalsIgnoreCase(predictedCatName))
                    .findFirst()
                    .orElseGet(() -> {
                        CategoryEntity newCat = CategoryEntity.builder()
                                .categoryName(predictedCatName)
                                .icon("Zap")
                                .type("EXPENSE")
                                .profile(profile)
                                .build();
                        return categoryRepository.save(newCat);
                    });
            expenseDto.setCategoryId(category.getId());
            expenseDto.setCategoryname(category.getCategoryName());
        }

        // Fixed: Ensure the category belongs to the current user's profile
        CategoryEntity category = categoryRepository.findByIdAndProfileId(expenseDto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or unauthorized"));

        ExpenseEntity newexpense = expenseMapper.toExpenseEntity(expenseDto, profile, category);
        expenseRepository.save(newexpense);

        return expenseMapper.toExpenseDto(newexpense);
    }

    /**
     * Add expense from an AI-parsed transaction.
     * Automatically finds or creates the target category.
     */
    public ExpenseDto addExpenseFromAi(AiTransactionDto txn) {
        ProfileEntity profile = profileService.getCurrentProfile();

        String catName = txn.getCategory() != null ? txn.getCategory() : "Other";
        CategoryEntity category = categoryRepository.findByProfileId(profile.getId()).stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(catName))
                .findFirst()
                .orElseGet(() -> {
                    CategoryEntity newCat = CategoryEntity.builder()
                            .categoryName(catName)
                            .icon("Zap")
                            .type("EXPENSE")
                            .profile(profile)
                            .build();
                    return categoryRepository.save(newCat);
                });

        LocalDate date;
        try {
            date = LocalDate.parse(txn.getDate());
        } catch (Exception e) {
            date = LocalDate.now();
        }

        ExpenseDto dto = ExpenseDto.builder()
                .name(txn.getDescription() != null ? txn.getDescription() : "AI Entry")
                .amount(BigDecimal.valueOf(txn.getAmount()))
                .CategoryId(category.getId())
                .categoryname(category.getCategoryName())
                .date(date)
                .icon("Zap")
                .build();

        ExpenseEntity entity = expenseMapper.toExpenseEntity(dto, profile, category);
        expenseRepository.save(entity);
        return expenseMapper.toExpenseDto(entity);
    }

    // Retrieve all expenses for current month
    public List<ExpenseDto> getAllExpenseOfCurrentMonth() {
        ProfileEntity profile = profileService.getCurrentProfile();

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        List<ExpenseEntity> expenses = expenseRepository.findByProfileAndDateBetween(profile, startDate, endDate);

        return expenses.stream()
                .map(expenseMapper::toExpenseDto)
                .collect(Collectors.toList());
    }

    public List<ExpenseDto> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findByProfileAndDateBetween(profile, startDate, endDate);
        return expenses.stream().map(expenseMapper::toExpenseDto).collect(Collectors.toList());
    }

    public Page<ExpenseDto> getPagedExpenses(Pageable pageable) {
        ProfileEntity p = profileService.getCurrentProfile();
        return expenseRepository.findByProfileId(p.getId(), pageable).map(expenseMapper::toExpenseDto);
    }

    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity expense = expenseRepository.findById(expenseId).orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("You cannot delete this");
        }
        expenseRepository.delete(expense);
    }

    // Get latest 5
    public List<ExpenseDto> getLatest5ExpensesOfCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());

        return expenses.stream()
                .map(expenseMapper::toExpenseDto)
                .collect(Collectors.toList());
    }

    // Get total expenses
    public BigDecimal gettotalExpense() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        if (total != null) {
            return total;
        }
        return BigDecimal.ZERO;
    }

    // Filter expenses
    public List<ExpenseDto> filterexpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> lst = expenseRepository.findByProfileIdAndDateBetweenAndExpensenameContainingIgnoreCase(
                profile.getId(), startDate, endDate, keyword, sort);

        return lst.stream()
                .map(expenseMapper::toExpenseDto)
                .collect(Collectors.toList());
    }

    // Daily summary helper
    public List<ExpenseDto> getexpensesforuserondate(Long profileId, LocalDate date) {
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDate(profileId, date);
        return list.stream().map(expenseMapper::toExpenseDto).collect(Collectors.toList());
    }
}
