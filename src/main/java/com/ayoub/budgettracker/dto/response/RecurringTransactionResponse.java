package com.ayoub.budgettracker.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionResponse(
        String description,
        BigDecimal monthlyAmount,
        String type,
        String categoryName,
        String categoryColor,
        String categoryIcon,
        String frequency,
        LocalDate lastDate
) {}
