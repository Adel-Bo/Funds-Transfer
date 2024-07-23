package com.funds.transfer.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.funds.transfer.model.Account;
import com.funds.transfer.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private Logger log;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    public void setUp() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
    }

    @Test
    public void testTransferFunds_Success() {
        Long fromAccountId = 1L, fromOwnerId = 1L;
        Long toAccountId = 2L, toOwnerId = 2L;
        Double amount = 100.0;

        Account fromAccount = new Account(fromAccountId, fromOwnerId, "USD", 200.0);
        Account toAccount = new Account(toAccountId, toOwnerId, "EUR", 100.0);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
        when(exchangeRateService.getExchangeRate("USD", "EUR")).thenReturn(0.85);

        CompletableFuture<Integer> result = accountService.transferFunds(fromAccountId, toAccountId, amount);

        assertEquals(1, result.join());
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

        Account fromAccount = new Account(fromAccountId, fromOwnerId, "USD", 200.0);
        Account toAccount = new Account(toAccountId, toOwnerId, "EUR", 100.0);

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        CompletableFuture<Integer> result = accountService.transferFunds(fromAccountId, toAccountId, amount);

        assertEquals(-1, result.join());
        verify(accountRepository, never()).save(fromAccount);
        verify(accountRepository, never()).save(toAccount);
    }

    @Test
    public void testTransferFunds_AccountNotFound() {
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        Double amount = 100.0;

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.empty());

        CompletableFuture<Integer> result = accountService.transferFunds(fromAccountId, toAccountId, amount);

        assertEquals(-1, result.join());
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
}