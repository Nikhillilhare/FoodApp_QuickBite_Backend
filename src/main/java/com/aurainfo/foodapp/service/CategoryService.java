package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.Category;
import com.aurainfo.foodapp.entity.CategoryStatus;

import java.util.List;

public interface CategoryService {

    Category createCategory(Category category);

    Category getCategoryById(Long categoryId);

    List<Category> getAllCategories();

    List<Category> getActiveCategories();

    Category updateCategory(Long categoryId, Category updatedCategory);

    void deactivateCategory(Long categoryId);

    void activateCategory(Long categoryId);

    boolean existsByName(String name);
}