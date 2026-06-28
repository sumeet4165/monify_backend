package com.example.MONEYMANAGER.mapper;

import com.example.MONEYMANAGER.dto.ExpenseDto;
import com.example.MONEYMANAGER.entity.CategoryEntity;
import com.example.MONEYMANAGER.entity.ExpenseEntity;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import org.springframework.stereotype.Service;

@Service
public class ExpenseMapper {

    public ExpenseEntity toExpenseEntity(ExpenseDto expenseDto, ProfileEntity profileEntity, CategoryEntity categoryEntity) {
        return ExpenseEntity.builder()
                .expensename(expenseDto.getName())
                .icon(expenseDto.getIcon())
                .amount(expenseDto.getAmount())
                .date(expenseDto.getDate())
                .category(categoryEntity)
                .profile(profileEntity)
                .build();
    }

    public ExpenseDto toExpenseDto(ExpenseEntity expenseEntity) {
        return ExpenseDto.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getExpensename())
                .icon(expenseEntity.getIcon())
                .CategoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null)
                .categoryname(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getCategoryName() : "N/A")
                .amount(expenseEntity.getAmount())
                .date(expenseEntity.getDate())
                .createdat(expenseEntity.getCreatetime())
                .updatedat(expenseEntity.getUpdatetime())
                .build();
    }
}
