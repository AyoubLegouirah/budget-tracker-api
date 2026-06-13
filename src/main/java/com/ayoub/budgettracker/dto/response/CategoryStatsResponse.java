package com.ayoub.budgettracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryStatsResponse {
    private String categoryName;
    private String color;
    private BigDecimal total;
}
