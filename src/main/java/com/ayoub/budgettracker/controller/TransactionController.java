package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.dto.response.TransactionResponse;
import com.ayoub.budgettracker.entity.Account;
import com.ayoub.budgettracker.entity.Transaction;
import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.mapper.TransactionMapper;
import com.ayoub.budgettracker.service.AccountService;
import com.ayoub.budgettracker.service.CategoryService;
import com.ayoub.budgettracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.ayoub.budgettracker.entity.Category;
import com.ayoub.budgettracker.entity.Account;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionMapper transactionMapper;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionMapper.toResponseList(transactionService.findByUserId(user.getId())));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionResponse>> getByType(@PathVariable String type,
                                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionMapper.toResponseList(transactionService.findByUserIdAndType(user.getId(), type)));
    }

    @GetMapping("/period")
    public ResponseEntity<List<TransactionResponse>> getByPeriod(@RequestParam LocalDate from,
                                                                   @RequestParam LocalDate to,
                                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionMapper.toResponseList(transactionService.findByUserIdAndDateBetween(user.getId(), from, to)));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody Transaction transaction,
                                                    @AuthenticationPrincipal User user) {
        Account account = accountService.findByIdAndUserId(transaction.getAccount().getId(), user.getId());
        Category category = categoryService.findByIdAndUserId(transaction.getCategory().getId(), user.getId());

        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setCategory(category);

        return ResponseEntity.ok(transactionMapper.toResponse(transactionService.save(transaction)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}