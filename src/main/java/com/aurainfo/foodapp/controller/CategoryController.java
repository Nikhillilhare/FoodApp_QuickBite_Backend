package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.CategoryRequest;
import com.aurainfo.foodapp.dto.response.CategoryResponse;
import com.aurainfo.foodapp.entity.Category;
import com.aurainfo.foodapp.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // =====================================================
    // PUBLIC CATEGORY API
    // =====================================================

    @GetMapping("/api/categories")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {

        List<CategoryResponse> response =
                categoryService.getActiveCategories()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // ADMIN CATEGORY APIs
    // =====================================================

    @PostMapping("/api/admin/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(
                        request.getDescription() != null
                                ? request.getDescription().trim()
                                : null
                )
                .build();

        Category savedCategory =
                categoryService.createCategory(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponse(savedCategory));
    }

    @GetMapping("/api/admin/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> response =
                categoryService.getAllCategories()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/categories/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable Long categoryId
    ) {

        Category category =
                categoryService.getCategoryById(categoryId);

        return ResponseEntity.ok(
                mapToResponse(category)
        );
    }

    @PutMapping("/api/admin/categories/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request
    ) {

        Category updatedCategory = Category.builder()
                .name(request.getName().trim())
                .description(
                        request.getDescription() != null
                                ? request.getDescription().trim()
                                : null
                )
                .build();

        Category category =
                categoryService.updateCategory(
                        categoryId,
                        updatedCategory
                );

        return ResponseEntity.ok(
                mapToResponse(category)
        );
    }

    @PatchMapping(
            "/api/admin/categories/{categoryId}/deactivate"
    )
    public ResponseEntity<String> deactivateCategory(
            @PathVariable Long categoryId
    ) {

        categoryService.deactivateCategory(categoryId);

        return ResponseEntity.ok(
                "Category deactivated successfully"
        );
    }

    @PatchMapping(
            "/api/admin/categories/{categoryId}/activate"
    )
    public ResponseEntity<String> activateCategory(
            @PathVariable Long categoryId
    ) {

        categoryService.activateCategory(categoryId);

        return ResponseEntity.ok(
                "Category activated successfully"
        );
    }

    // =====================================================
    // MAPPER
    // =====================================================

    private CategoryResponse mapToResponse(
            Category category
    ) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .build();
    }
}