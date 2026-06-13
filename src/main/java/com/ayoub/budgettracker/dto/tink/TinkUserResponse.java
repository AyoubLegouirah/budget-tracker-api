package com.ayoub.budgettracker.dto.tink;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class TinkUserResponse {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("external_user_id")
    private String externalUserId;
}
