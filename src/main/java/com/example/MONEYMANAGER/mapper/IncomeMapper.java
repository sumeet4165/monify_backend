package com.example.MONEYMANAGER.mapper;

import com.example.MONEYMANAGER.dto.IncomeDto;
import com.example.MONEYMANAGER.entity.CategoryEntity;
import com.example.MONEYMANAGER.entity.IncomeEntity;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import org.springframework.stereotype.Service;

@Service
public class IncomeMapper {

    public IncomeEntity toIncomeEntity(IncomeDto incomeDto, ProfileEntity profileEntity, CategoryEntity categoryEntity) {
        return IncomeEntity.builder()
                .incomename(incomeDto.getName())
                .icon(incomeDto.getIcon())
                .amount(incomeDto.getAmount())
                .date(incomeDto.getDate())
                .category(categoryEntity)
                .profile(profileEntity)
                .build();
    }

    public IncomeDto toIncomeDto(IncomeEntity incomeEntity) {
        return IncomeDto.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getIncomename())
                .icon(incomeEntity.getIcon())
                .CategoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null)
                .categoryname(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getCategoryName() : "N/A")
                .amount(incomeEntity.getAmount())
                .date(incomeEntity.getDate())
                .createdat(incomeEntity.getCreatetime())
                .updatedat(incomeEntity.getUpdatetime())
                .build();
    }
}
