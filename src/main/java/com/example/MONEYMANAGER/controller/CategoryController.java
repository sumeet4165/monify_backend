package com.example.MONEYMANAGER.controller;

import com.example.MONEYMANAGER.dto.CategoryDto;
import com.example.MONEYMANAGER.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> saveCategory(@RequestBody CategoryDto categoryDto) {
        CategoryDto saved = categoryService.saveCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getCategoriesforCurrentUser();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByTypeForCurrenuser(@PathVariable String type) {
        List<CategoryDto> list = categoryService.getCategoriesbytypeforcurrentuser(type);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{categoryid}")
    public ResponseEntity<CategoryDto> updatecategory(@PathVariable Long categoryid, @RequestBody CategoryDto categoryDto) {
        CategoryDto updated = categoryService.updateCategory(categoryid, categoryDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{categoryid}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryid) {
        categoryService.deleteCategory(categoryid);
        return ResponseEntity.noContent().build();
    }
}
