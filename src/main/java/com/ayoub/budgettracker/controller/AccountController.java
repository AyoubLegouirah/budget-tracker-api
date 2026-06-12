package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.dto.response.AccountResponse;
import com.ayoub.budgettracker.entity.Account;
import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.mapper.AccountMapper;
import com.ayoub.budgettracker.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountMapper.toResponseList(accountService.findByUserId(user.getId())));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody Account account,
                                                   @AuthenticationPrincipal User user) {
        account.setUser(user);
        return ResponseEntity.ok(accountMapper.toResponse(accountService.save(account)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}