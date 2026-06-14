package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.dto.response.RecurringTransactionResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final Set<String> EXCLUDED_KEYWORDS = Set.of("retrait", "virement", "remboursement");

    public List<RecurringTransactionResponse> detectRecurring(UUID userId) {
        List<Transaction> all = transactionRepository.findByUserIdOrderByDateDesc(userId);

        Map<String, List<Transaction>> grouped = all.stream()
                .filter(t -> {
                    String desc = t.getDescription().toLowerCase();
                    return EXCLUDED_KEYWORDS.stream().noneMatch(desc::contains);
                })
                .collect(Collectors.groupingBy(t -> t.getDescription().toLowerCase().trim()));

        List<RecurringTransactionResponse> result = new ArrayList<>();

        for (List<Transaction> txs : grouped.values()) {
            if (txs.size() < 3) continue;

            List<BigDecimal> sortedAmounts = txs.stream()
                    .map(Transaction::getAmount)
                    .sorted()
                    .toList();
            BigDecimal median = sortedAmounts.get(sortedAmounts.size() / 2);
            BigDecimal tolerance = median.multiply(BigDecimal.valueOf(0.05));

            List<Transaction> similar = txs.stream()
                    .filter(t -> t.getAmount().subtract(median).abs().compareTo(tolerance) <= 0)
                    .toList();

            long distinctMonths = similar.stream()
                    .map(t -> t.getDate().withDayOfMonth(1))
                    .distinct()
                    .count();

            if (distinctMonths < 3) continue;

            String frequency = computeFrequency(similar);
            if (!frequency.equals("MONTHLY") && !frequency.equals("ANNUAL")) continue;

            Transaction latest = similar.stream()
                    .max(Comparator.comparing(Transaction::getDate))
                    .orElseThrow();

            result.add(new RecurringTransactionResponse(
                    latest.getDescription(),
                    median,
                    latest.getType(),
                    latest.getCategory().getName(),
                    latest.getCategory().getColor(),
                    latest.getCategory().getIcon(),
                    frequency,
                    latest.getDate()
            ));
        }

        result.sort(Comparator.comparing(RecurringTransactionResponse::monthlyAmount).reversed());
        return result;
    }

    private String computeFrequency(List<Transaction> txs) {
        List<LocalDate> months = txs.stream()
                .map(t -> t.getDate().withDayOfMonth(1))
                .distinct()
                .sorted()
                .toList();

        if (months.size() < 2) return "IRREGULAR";

        double total = 0;
        for (int i = 1; i < months.size(); i++) {
            total += ChronoUnit.MONTHS.between(months.get(i - 1), months.get(i));
        }
        double avg = total / (months.size() - 1);

        if (avg <= 1.5)  return "MONTHLY";
        if (avg <= 2.5)  return "BIMONTHLY";
        if (avg <= 4.5)  return "QUARTERLY";
        if (avg <= 8.0)  return "SEMI_ANNUAL";
        if (avg <= 13.0) return "ANNUAL";
        return "IRREGULAR";
    }
}