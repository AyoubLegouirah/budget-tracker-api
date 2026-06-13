package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.entity.Transaction;
import com.ayoub.budgettracker.repository.TransactionRepository;
import com.ayoub.budgettracker.specification.TransactionSpec;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> findByUserId(UUID userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Page<Transaction> filter(UUID userId, String type, UUID categoryId,
                                     LocalDate from, LocalDate to, int page, int size) {
        Specification<Transaction> spec = TransactionSpec.belongsToUser(userId);
        if (type != null)       spec = spec.and(TransactionSpec.hasType(type));
        if (categoryId != null) spec = spec.and(TransactionSpec.hasCategory(categoryId));
        if (from != null)       spec = spec.and(TransactionSpec.fromDate(from));
        if (to != null)         spec = spec.and(TransactionSpec.toDate(to));
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return transactionRepository.findAll(spec, pageable);
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

    public Transaction updateCategory(UUID transactionId, UUID userId,
                                       com.ayoub.budgettracker.entity.Category category) {
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new RuntimeException("Transaction introuvable"));
        tx.setCategory(category);
        return transactionRepository.save(tx);
    }
}