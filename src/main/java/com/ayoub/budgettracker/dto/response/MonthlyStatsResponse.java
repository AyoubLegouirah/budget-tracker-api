package com.ayoub.budgettracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyStatsResponse {
    private String month; // format "2025-01"
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
}
