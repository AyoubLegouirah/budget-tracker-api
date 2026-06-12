package com.ayoub.budgettracker.mapper;

import com.ayoub.budgettracker.dto.response.AccountResponse;
import com.ayoub.budgettracker.entity.Account;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        return response;
    }

    public List<AccountResponse> toResponseList(List<Account> accounts) {
        return accounts.stream().map(this::toResponse).toList();
    }
}