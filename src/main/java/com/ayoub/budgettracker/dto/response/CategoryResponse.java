package com.ayoub.budgettracker.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class CategoryResponse {
    private UUID id;
    private String name;
    private String color;
    private String type;
    private String icon;
}