package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.dto.response.CategoryResponse;
import com.ayoub.budgettracker.entity.Category;
import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.mapper.CategoryMapper;
import com.ayoub.budgettracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryMapper.toResponseList(categoryService.findByUserId(user.getId())));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody Category category,
                                                    @AuthenticationPrincipal User user) {
        category.setUser(user);
        return ResponseEntity.ok(categoryMapper.toResponse(categoryService.save(category)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}