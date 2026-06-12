package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.entity.Transaction;
import com.ayoub.budgettracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> findByUserId(UUID userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<Transaction> findByUserIdAndType(UUID userId, String type) {
        return transactionRepository.findByUserIdAndTypeOrderByDateDesc(userId, type);
    }

    public List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate from, LocalDate to) {
        return transactionRepository.findByUserIdAndDateBetween(userId, from, to);
    }

    public Transaction findByIdAndUserId(UUID id, UUID userId) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Transaction introuvable"));
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void delete(UUID id) {
        transactionRepository.deleteById(id);
    }
}