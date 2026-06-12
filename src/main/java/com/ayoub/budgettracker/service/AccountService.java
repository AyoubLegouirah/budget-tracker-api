package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.entity.Account;
import com.ayoub.budgettracker.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> findByUserId(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account findByIdAndUserId(UUID id, UUID userId) {
        return accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable"));
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public void delete(UUID id) {
        accountRepository.deleteById(id);
    }
}