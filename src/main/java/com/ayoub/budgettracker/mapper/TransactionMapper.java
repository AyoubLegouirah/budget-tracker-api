package com.ayoub.budgettracker.mapper;

import com.ayoub.budgettracker.dto.response.TransactionResponse;
import com.ayoub.budgettracker.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setNote(transaction.getNote());
        response.setType(transaction.getType());
        response.setDate(transaction.getDate());
        response.setCreatedAt(transaction.getCreatedAt());

        if (transaction.getAccount() != null) {
            response.setAccountId(transaction.getAccount().getId());
            response.setAccountName(transaction.getAccount().getName());
        }

        if (transaction.getCategory() != null) {
            response.setCategoryId(transaction.getCategory().getId());
            response.setCategoryName(transaction.getCategory().getName());
            response.setCategoryColor(transaction.getCategory().getColor());
            response.setCategoryIcon(transaction.getCategory().getIcon());
        }

        return response;
    }

    public List<TransactionResponse> toResponseList(List<Transaction> transactions) {
        return transactions.stream().map(this::toResponse).toList();
    }
}