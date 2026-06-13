package com.ayoub.budgettracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
}
