package com.example.MONEYMANAGER.mapper;

import com.example.MONEYMANAGER.dto.CategoryDto;
import com.example.MONEYMANAGER.entity.CategoryEntity;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {

    public CategoryEntity toCategoryEntity(CategoryDto categoryDto, ProfileEntity profileEntity) {
        return CategoryEntity.builder()
                .categoryName(categoryDto.getName())
                .icon(categoryDto.getIcon())
                .profile(profileEntity)
                .type(categoryDto.getType())
                .build();
    }

    public CategoryDto toCategoryDto(CategoryEntity categoryEntity) {
        return CategoryDto.builder()
                .id(categoryEntity.getId())
                .profileId(categoryEntity.getProfile() != null ? categoryEntity.getProfile().getId() : null)
                .name(categoryEntity.getCategoryName())
                .icon(categoryEntity.getIcon())
                .type(categoryEntity.getType())
                .createdAt(categoryEntity.getCreateTime())
                .updatedAt(categoryEntity.getUpdateTime())
                .build();
    }
}
