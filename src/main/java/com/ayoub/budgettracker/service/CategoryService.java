package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.entity.Category;
import com.ayoub.budgettracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findByUserId(UUID userId) {
        return categoryRepository.findByUserId(userId);
    }

    public Category findByIdAndUserId(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void delete(UUID id) {
        categoryRepository.deleteById(id);
    }
}