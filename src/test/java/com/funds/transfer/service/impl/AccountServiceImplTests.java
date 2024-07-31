package com.funds.transfer.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.funds.transfer.model.Account;
import com.funds.transfer.repository.AccountRepository;
import com.funds.transfer.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private AccountServiceImpl accountService = new AccountServiceImpl();

    @BeforeEach
    public void setUp() {
        accountService.maxThreads = 10;
    }

    @Test
    public void testTransferFunds_Success() {
        Long fromAccountId = 1L, fromOwnerId = 1L;
        Long toAccountId = 2L, toOwnerId = 2L;
        Double amount = 100.0;

        Account fromAccount = getCreditAccount(fromAccountId, fromOwnerId, "USD", 200.0);
        Account toAccount = getDebitAccount(toAccountId, toOwnerId, "EUR", 100.0);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
        when(exchangeRateService.getExchangeRate("USD", "EUR")).thenReturn(0.85);

        CompletableFuture<Boolean> result = accountService.transferFunds(fromAccountId, toAccountId, amount);

        assertTrue(result.join());
        assertEquals(100.0, fromAccount.getBalance());
        assertEquals(185.0, toAccount.getBalance());
        verify(accountRepository, times(1)).save(fromAccount);
        verify(accountRepository, times(1)).save(toAccount);
    }

    @Test
    public void testTransferFunds_InsufficientFunds() {
        Long fromAccountId = 1L, fromOwnerId = 1L;
        Long toAccountId = 2L, toOwnerId = 2L;
        Double amount = 300.0;

        Account fromAccount = getCreditAccount(fromAccountId, fromOwnerId, "USD", 200.0);
        Account toAccount = getDebitAccount(toAccountId, toOwnerId, "EUR", 100.0);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        CompletableFuture<Boolean> result = accountService.transferFunds(fromAccountId, toAccountId, amount);

        assertFalse(result.join());
        verify(accountRepository, never()).save(fromAccount);
        verify(accountRepository, never()).save(toAccount);
    }

    @Test
    public void testTransferFunds_AccountNotFound() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        Double amount = 100.0;

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.empty());

        CompletableFuture<Boolean> result = accountService.transferFunds(fromAccountId, toAccountId, amount);

        assertFalse(result.join());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testSaveAccount() {
        Account account = new Account();
        account.setOwnerId(3L);
        account.setCurrency("USD");
        account.setBalance(5000.0);

        accountService.saveAccount(account);

        assertEquals(5000.0, account.getBalance());
        assertEquals(3L, account.getOwnerId());
        assertEquals("USD", account.getCurrency());

        verify(accountRepository, times(1)).save(account);
    }

    private Account getDebitAccount(Long fromAccountId, Long fromOwnerId, String currency, Double balance) {
        return new Account(fromAccountId, fromOwnerId, currency, balance);
    }

    private Account getCreditAccount(Long fromAccountId, Long fromOwnerId, String currency, Double balance) {
        return new Account(fromAccountId, fromOwnerId, currency, balance);
    }
}