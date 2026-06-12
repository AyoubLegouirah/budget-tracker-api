package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.entity.Category;
import com.ayoub.budgettracker.entity.User;
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

    @GetMapping
    public ResponseEntity<List<Category>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.findByUserId(user.getId()));
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category,
                                           @AuthenticationPrincipal User user) {
        category.setUser(user);
        return ResponseEntity.ok(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}