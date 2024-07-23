package com.funds.transfer.service.impl;

import com.funds.transfer.model.Account;
import com.funds.transfer.repository.AccountRepository;
import com.funds.transfer.exception.AccountNotFoundException;
import com.funds.transfer.exception.InsufficientFundsException;
import com.funds.transfer.service.AccountService;
import com.funds.transfer.service.ExchangeRateService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Transactional
    @Async
    public CompletableFuture<Integer> transferFunds(Long fromAccountId, Long toAccountId, Double amount) {
        return CompletableFuture.supplyAsync(() -> accountRepository.findById(fromAccountId)
                        .orElseThrow(() -> new AccountNotFoundException("Debit account with id " + fromAccountId + " not found")), executorService)
                .thenCombineAsync(CompletableFuture.supplyAsync(() -> accountRepository.findById(toAccountId)
                        .orElseThrow(() -> new AccountNotFoundException("Credit account with id " + toAccountId + " not found")), executorService), (fromAccount, toAccount) -> {

                    if (fromAccount.getBalance() < amount) {
                        throw new InsufficientFundsException("Insufficient funds in debit account");
                    }

                    Double exchangeRate = exchangeRateService.getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency());
                    Double convertedAmount = amount * exchangeRate;

                    fromAccount.setBalance(fromAccount.getBalance() - amount);
                    toAccount.setBalance(toAccount.getBalance() + convertedAmount);

                    accountRepository.save(fromAccount);
                    accountRepository.save(toAccount);

                    return 1;
                }, executorService).exceptionally(ex -> {

                    log.error("\u001B[31m" + "Transfer Processing Error: [{}]" + "\u001B[0m", ex.getMessage());
                    return -1;
                });
    }


    public void saveAccount(Account account) {
        accountRepository.save(account);
    }
}

