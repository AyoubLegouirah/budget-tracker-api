package com.ayoub.budgettracker.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String description;
    private String note;
    private String type;
    private LocalDate date;
    private UUID accountId;
    private String accountName;
    private UUID categoryId;
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private LocalDateTime createdAt;
}