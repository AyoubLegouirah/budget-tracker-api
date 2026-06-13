package com.ayoub.budgettracker.specification;

import com.ayoub.budgettracker.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TransactionSpec {

    private TransactionSpec() {}

    public static Specification<Transaction> belongsToUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasType(String type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasCategory(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> fromDate(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    public static Specification<Transaction> toDate(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), to);
    }
}
