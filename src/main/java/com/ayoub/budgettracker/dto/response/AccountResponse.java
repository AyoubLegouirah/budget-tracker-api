package com.ayoub.budgettracker.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountResponse {
    private UUID id;
    private String name;
    private BigDecimal balance;
    private String currency;
}