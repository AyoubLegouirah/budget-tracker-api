package com.ayoub.budgettracker.dto.tink;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class TinkTransactionItem {

    private String id;
    private String accountId;
    private TinkAmount amount;
    private TinkCategories categories;
    private TinkDates dates;
    private TinkDescriptions descriptions;
    private String status;
    private String type;

    @Getter @Setter @NoArgsConstructor
    public static class TinkAmount {
        private String currencyCode;
        private TinkAmountValue value;
    }

    @Getter @Setter @NoArgsConstructor
    public static class TinkAmountValue {
        private long unscaledValue;
        private int scale;
    }

    @Getter @Setter @NoArgsConstructor
    public static class TinkCategories {
        private TinkCategory pfm;
    }

    @Getter @Setter @NoArgsConstructor
    public static class TinkCategory {
        private String id;
        private String name;
    }

    @Getter @Setter @NoArgsConstructor
    public static class TinkDates {
        private String booked;
    }

    @Getter @Setter @NoArgsConstructor
    public static class TinkDescriptions {
        private String display;
        private String original;
    }
}
