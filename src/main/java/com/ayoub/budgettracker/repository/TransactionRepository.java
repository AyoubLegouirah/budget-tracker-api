package com.ayoub.budgettracker.repository;

import com.ayoub.budgettracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByUserIdOrderByDateDesc(UUID userId);
    List<Transaction> findByUserIdAndTypeOrderByDateDesc(UUID userId, String type);
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
    Optional<Transaction> findByTinkId(String tinkId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.date BETWEEN :from AND :to ORDER BY t.date DESC")
    List<Transaction> findByUserIdAndDateBetween(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT t.category.name, t.category.color, SUM(t.amount) " +
           "FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.date BETWEEN :from AND :to " +
           "GROUP BY t.category.name, t.category.color " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> findExpensesByCategory(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.date >= :from")
    List<Transaction> findByUserIdAndDateAfter(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from);
}