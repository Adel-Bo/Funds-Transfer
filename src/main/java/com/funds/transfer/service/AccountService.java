package com.funds.transfer.service;

import com.funds.transfer.model.Account;

import java.util.concurrent.CompletableFuture;

public interface AccountService {

    CompletableFuture<Boolean> transferFunds(Long fromAccountId, Long toAccountId, Double amount);
    void saveAccount(Account account);
}
