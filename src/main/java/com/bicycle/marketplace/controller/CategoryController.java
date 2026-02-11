package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.CategoryCreationRequest;
import com.bicycle.marketplace.dto.request.CategoryUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.CategoryResponse;
import com.bicycle.marketplace.entities.Category;
import com.bicycle.marketplace.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    ApiResponse<CategoryResponse> createCategory(@RequestBody CategoryCreationRequest request) {
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.createCategory(request));
        apiResponse.setMessage("Category created successfully");
        return apiResponse;
    }

    @PutMapping("/{categoryId}")
    ApiResponse<CategoryResponse> updateCategory(@PathVariable int categoryId, @RequestBody CategoryUpdateRequest request) {
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.updateCategory(categoryId, request));
        apiResponse.setMessage("Category updated successfully");
        return apiResponse;
    }

    @GetMapping("/{categoryId}")
    ApiResponse<CategoryResponse> getCategoryById(@PathVariable int categoryId) {
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.getCategoryById(categoryId));
        apiResponse.setMessage("Category fetched successfully");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Category>> getAllCategories() {
        ApiResponse<List<Category>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.getAllCategories());
        apiResponse.setMessage("Categories fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{categoryId}")
    ApiResponse<String> deleteCategory(@PathVariable int categoryId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.deleteCategory(categoryId));
        return apiResponse;
    }


}
