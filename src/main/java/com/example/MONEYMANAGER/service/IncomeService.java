package com.example.MONEYMANAGER.service;

import com.example.MONEYMANAGER.dto.IncomeDto;
import com.example.MONEYMANAGER.dto.AiTransactionDto;
import com.example.MONEYMANAGER.entity.CategoryEntity;
import com.example.MONEYMANAGER.entity.IncomeEntity;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.mapper.IncomeMapper;
import com.example.MONEYMANAGER.repository.CategoryRepository;
import com.example.MONEYMANAGER.repository.IncomeRepository;
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
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final IncomeMapper incomeMapper;

    public IncomeDto AddIncome(IncomeDto incomeDto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        // Fixed: Ensure the category belongs to the current user's profile
        CategoryEntity category = categoryRepository.findByIdAndProfileId(incomeDto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or unauthorized"));

        IncomeEntity newincome = incomeMapper.toIncomeEntity(incomeDto, profile, category);
        incomeRepository.save(newincome);

        return incomeMapper.toIncomeDto(newincome);
    }

    /**
     * Add income from an AI-parsed transaction.
     * Automatically finds or creates the target category.
     */
    public IncomeDto addIncomeFromAi(AiTransactionDto txn) {
        ProfileEntity profile = profileService.getCurrentProfile();

        // Find or create category
        String catName = txn.getCategory() != null ? txn.getCategory() : "Other";
        CategoryEntity category = categoryRepository.findByProfileId(profile.getId()).stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(catName))
                .findFirst()
                .orElseGet(() -> {
                    CategoryEntity newCat = CategoryEntity.builder()
                            .categoryName(catName)
                            .icon("Zap")
                            .type("INCOME")
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

        IncomeDto dto = IncomeDto.builder()
                .name(txn.getDescription() != null ? txn.getDescription() : "AI Entry")
                .amount(BigDecimal.valueOf(txn.getAmount()))
                .CategoryId(category.getId())
                .categoryname(category.getCategoryName())
                .date(date)
                .icon("Zap")
                .build();

        IncomeEntity entity = incomeMapper.toIncomeEntity(dto, profile, category);
        incomeRepository.save(entity);
        return incomeMapper.toIncomeDto(entity);
    }

    // Retrieve all incomes for current month
    public List<IncomeDto> getAllIncomeOfCurrentMonth() {
        ProfileEntity profile = profileService.getCurrentProfile();

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        List<IncomeEntity> incomes = incomeRepository.findByProfileAndDateBetween(profile, startDate, endDate);

        return incomes.stream()
                .map(incomeMapper::toIncomeDto)
                .collect(Collectors.toList());
    }

    public List<IncomeDto> getIncomesByDateRange(LocalDate startDate, LocalDate endDate) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomes = incomeRepository.findByProfileAndDateBetween(profile, startDate, endDate);
        return incomes.stream().map(incomeMapper::toIncomeDto).collect(Collectors.toList());
    }

    public Page<IncomeDto> getPagedIncomes(Pageable pageable) {
        ProfileEntity p = profileService.getCurrentProfile();
        return incomeRepository.findByProfileId(p.getId(), pageable).map(incomeMapper::toIncomeDto);
    }

    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity income = incomeRepository.findById(incomeId).orElseThrow(() -> new RuntimeException("Income not found"));

        if (!income.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("You cannot delete this");
        }
        incomeRepository.delete(income);
    }

    // Get latest 5
    public List<IncomeDto> getLatest5IncomesOfCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());

        return incomes.stream()
                .map(incomeMapper::toIncomeDto)
                .collect(Collectors.toList());
    }

    // Get total income
    public BigDecimal gettotalIncome() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        if (total != null) {
            return total;
        }
        return BigDecimal.ZERO;
    }

    // Filter incomes
    public List<IncomeDto> filterincomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> lst = incomeRepository.findByProfileIdAndDateBetweenAndIncomenameContainingIgnoreCase(
                profile.getId(), startDate, endDate, keyword, sort);

        return lst.stream()
                .map(incomeMapper::toIncomeDto)
                .collect(Collectors.toList());
    }
}
