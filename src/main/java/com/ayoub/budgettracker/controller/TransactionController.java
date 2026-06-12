package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.entity.Transaction;
import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.service.AccountService;
import com.ayoub.budgettracker.service.CategoryService;
import com.ayoub.budgettracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<Transaction>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.findByUserId(user.getId()));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Transaction>> getByType(@PathVariable String type,
                                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.findByUserIdAndType(user.getId(), type));
    }

    @GetMapping("/period")
    public ResponseEntity<List<Transaction>> getByPeriod(@RequestParam LocalDate from,
                                                          @RequestParam LocalDate to,
                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.findByUserIdAndDateBetween(user.getId(), from, to));
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction,
                                              @AuthenticationPrincipal User user) {
        transaction.setUser(user);
        return ResponseEntity.ok(transactionService.save(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}