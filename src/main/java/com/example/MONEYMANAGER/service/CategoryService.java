package com.example.MONEYMANAGER.service;

import com.example.MONEYMANAGER.dto.CategoryDto;
import com.example.MONEYMANAGER.entity.CategoryEntity;
import com.example.MONEYMANAGER.entity.ProfileEntity;
import com.example.MONEYMANAGER.mapper.CategoryMapper;
import com.example.MONEYMANAGER.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // Save category
    public CategoryDto saveCategory(CategoryDto categoryDto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByCategoryNameAndProfileId(categoryDto.getName(), profile.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }
        CategoryEntity categoryEntity = categoryMapper.toCategoryEntity(categoryDto, profile);
        categoryRepository.save(categoryEntity);
        return categoryMapper.toCategoryDto(categoryEntity);
    }

    // Get all categories for the currently authenticated user
    public List<CategoryDto> getCategoriesforCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();

        List<CategoryEntity> categoryEntities = categoryRepository.findByProfileId(profile.getId());

        if (categoryEntities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No categories found for this user");
        }

        return categoryEntities.stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    // Get categories by type for current user
    public List<CategoryDto> getCategoriesbytypeforcurrentuser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();

        List<CategoryEntity> categoryEntities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        if (categoryEntities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No categories found for this user");
        }
        return categoryEntities.stream().map(categoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    // Update Category
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existing = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        existing.setCategoryName(categoryDto.getName());
        existing.setIcon(categoryDto.getIcon());
        existing.setType(categoryDto.getType());
        categoryRepository.save(existing);
        return categoryMapper.toCategoryDto(existing);
    }

    // Delete Category (Fixed authorization bypass bug)
    public void deleteCategory(Long categoryId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existing = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found or access denied"));
        categoryRepository.delete(existing);
    }
}
