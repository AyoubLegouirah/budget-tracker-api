package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.dto.response.BalanceStatsResponse;
import com.ayoub.budgettracker.dto.response.CategoryStatsResponse;
import com.ayoub.budgettracker.dto.response.MonthlyStatsResponse;
import com.ayoub.budgettracker.entity.Transaction;
import com.ayoub.budgettracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    public List<CategoryStatsResponse> getExpensesByCategory(UUID userId) {
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        return transactionRepository.findExpensesByCategory(userId, from, to)
                .stream()
                .map(row -> new CategoryStatsResponse(
                        (String) row[0],
                        (String) row[1],
                        (BigDecimal) row[2]))
                .toList();
    }

    public List<MonthlyStatsResponse> getLast6Months(UUID userId) {
        LocalDate from = LocalDate.now().minusMonths(5).withDayOfMonth(1);

        Map<String, BigDecimal[]> byMonth = new LinkedHashMap<>();
        // Pre-fill all 6 months with zeros so months with no transactions appear too
        for (int i = 5; i >= 0; i--) {
            String key = LocalDate.now().minusMonths(i).format(MONTH_FMT);
            byMonth.put(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        for (Transaction t : transactionRepository.findByUserIdAndDateAfter(userId, from)) {
            String key = t.getDate().format(MONTH_FMT);
            BigDecimal[] totals = byMonth.get(key);
            if (totals == null) continue;
            if ("INCOME".equals(t.getType())) {
                totals[0] = totals[0].add(t.getAmount());
            } else {
                totals[1] = totals[1].add(t.getAmount());
            }
        }

        return byMonth.entrySet().stream()
                .map(e -> new MonthlyStatsResponse(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    public BalanceStatsResponse getBalance(UUID userId) {
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<Transaction> txs = transactionRepository.findByUserIdAndDateBetween(userId, from, to);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction t : txs) {
            if ("INCOME".equals(t.getType())) income = income.add(t.getAmount());
            else expense = expense.add(t.getAmount());
        }

        return new BalanceStatsResponse(income, expense, income.subtract(expense), txs.size());
    }
}
