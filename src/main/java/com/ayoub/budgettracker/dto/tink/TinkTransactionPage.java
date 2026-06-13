package com.ayoub.budgettracker.dto.tink;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class TinkTransactionPage {
    private String nextPageToken;
    private List<TinkTransactionItem> transactions;
}
