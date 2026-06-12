package com.ayoub.budgettracker.mapper;

import com.ayoub.budgettracker.dto.response.CategoryResponse;
import com.ayoub.budgettracker.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setColor(category.getColor());
        response.setIcon(category.getIcon());
        return response;
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return categories.stream().map(this::toResponse).toList();
    }
}