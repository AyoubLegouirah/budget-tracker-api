package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.dto.response.BalanceStatsResponse;
import com.ayoub.budgettracker.dto.response.CategoryStatsResponse;
import com.ayoub.budgettracker.dto.response.MonthlyStatsResponse;
import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/by-category")
    public ResponseEntity<List<CategoryStatsResponse>> byCategory(
            @RequestParam(defaultValue = "month") String period,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getExpensesByCategory(user.getId()));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyStatsResponse>> monthly(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getLast6Months(user.getId()));
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceStatsResponse> balance(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getBalance(user.getId()));
    }
}
