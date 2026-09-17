package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.Category;
import com.aurainfo.foodapp.entity.CategoryStatus;
import com.aurainfo.foodapp.repository.CategoryRepository;
import com.aurainfo.foodapp.service.AuditLogService;
import com.aurainfo.foodapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    @Override
    public Category createCategory(Category category) {


        validateCategory(category);

        String name = category.getName().trim();

        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Category already exists with name: " + name
            );
        }

        category.setName(name);

        if (category.getStatus() == null) {
            category.setStatus(CategoryStatus.ACTIVE);
        }

        Category savedCategory =
                categoryRepository.save(category);

        auditLogService.logCurrentAdminAction(
                "CREATE_CATEGORY",
                "Category",
                savedCategory.getId()
        );

        return savedCategory;    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long categoryId) {

        validateId(categoryId);

        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found with id: " + categoryId
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {

        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {

        return categoryRepository.findByStatus(
                CategoryStatus.ACTIVE
        );
    }

    @Override
    public Category updateCategory(
            Long categoryId,
            Category updatedCategory
    ) {

        validateId(categoryId);

        if (updatedCategory == null) {
            throw new IllegalArgumentException(
                    "Category data cannot be null"
            );
        }

        Category existingCategory =
                getCategoryById(categoryId);

        if (updatedCategory.getName() != null &&
                !updatedCategory.getName().isBlank()) {

            String newName =
                    updatedCategory.getName().trim();

            if (!newName.equalsIgnoreCase(
                    existingCategory.getName()
            ) &&
                    categoryRepository.existsByName(newName)) {

                throw new IllegalArgumentException(
                        "Category already exists with name: "
                                + newName
                );
            }

            existingCategory.setName(newName);
        }

        if (updatedCategory.getDescription() != null) {
            existingCategory.setDescription(
                    updatedCategory.getDescription().trim()
            );
        }

        Category savedCategory =
                categoryRepository.save(existingCategory);

        auditLogService.logCurrentAdminAction(
                "UPDATE_CATEGORY",
                "Category",
                savedCategory.getId()
        );

        return savedCategory;
    }

    @Override
    public void deactivateCategory(Long categoryId) {

        Category category = getCategoryById(categoryId);

        category.setStatus(CategoryStatus.INACTIVE);

        categoryRepository.save(category);

        auditLogService.logCurrentAdminAction(
                "DEACTIVATE_CATEGORY",
                "Category",
                category.getId()
        );
    }

    @Override
    public void activateCategory(Long categoryId) {

        Category category = getCategoryById(categoryId);

        category.setStatus(CategoryStatus.ACTIVE);

        categoryRepository.save(category);

        auditLogService.logCurrentAdminAction(
                "ACTIVATE_CATEGORY",
                "Category",
                category.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {

        if (name == null || name.isBlank()) {
            return false;
        }

        return categoryRepository.existsByName(
                name.trim()
        );
    }

    private void validateCategory(Category category) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category cannot be null"
            );
        }

        if (category.getName() == null ||
                category.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Category name is required"
            );
        }

        if (category.getName().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Category name cannot exceed 100 characters"
            );
        }

        if (category.getDescription() != null &&
                category.getDescription().length() > 255) {

            throw new IllegalArgumentException(
                    "Category description cannot exceed 255 characters"
            );
        }
    }

    private void validateId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Category ID must be greater than zero"
            );
        }
    }
}