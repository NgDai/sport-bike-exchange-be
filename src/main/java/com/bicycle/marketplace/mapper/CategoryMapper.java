package com.bicycle.marketplace.mapper;

import com.bicycle.marketplace.dto.request.CategoryCreationRequest;
import com.bicycle.marketplace.dto.request.CategoryUpdateRequest;
import com.bicycle.marketplace.dto.response.CategoryResponse;
import com.bicycle.marketplace.entities.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryCreationRequest request);
    CategoryResponse toCategoryResponse(Category category);
    void updateCategory(@MappingTarget Category category, CategoryUpdateRequest request);
}
