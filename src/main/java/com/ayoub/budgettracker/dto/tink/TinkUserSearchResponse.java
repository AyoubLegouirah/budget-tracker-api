package com.ayoub.budgettracker.dto.tink;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class TinkUserSearchResponse {
    private String nextPageToken;
    private List<TinkUserItem> users;

    @Getter @Setter @NoArgsConstructor
    public static class TinkUserItem {
        private String id;
        private String externalUserId;
    }
}
