package com.bicycle.marketplace.services;

import com.bicycle.marketplace.repository.ICategoryRepository;
import com.bicycle.marketplace.dto.request.CategoryCreationRequest;
import com.bicycle.marketplace.dto.request.CategoryUpdateRequest;
import com.bicycle.marketplace.dto.response.CategoryResponse;
import com.bicycle.marketplace.entities.Category;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryCreationRequest request) {
        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(int categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryMapper.updateCategory(category, request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public CategoryResponse getCategoryById(int categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCategory(int categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
        return "Category deleted successfully";
    }
}
