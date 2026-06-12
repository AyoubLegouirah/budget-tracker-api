package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.entity.Transaction;
import com.ayoub.budgettracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(transactionService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Transaction>> getByType(
            @PathVariable UUID userId,
            @PathVariable String type) {
        return ResponseEntity.ok(transactionService.findByUserIdAndType(userId, type));
    }

    @GetMapping("/user/{userId}/period")
    public ResponseEntity<List<Transaction>> getByPeriod(
            @PathVariable UUID userId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(transactionService.findByUserIdAndDateBetween(userId, from, to));
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.save(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}