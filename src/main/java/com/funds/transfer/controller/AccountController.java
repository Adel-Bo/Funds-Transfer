package com.funds.transfer.controller;

import com.funds.transfer.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/accounts")
public class AccountController implements ErrorController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/transfer")
    public ResponseEntity<String> transferFunds(@RequestParam Long fromAccountId, @RequestParam Long toAccountId,
                                                @RequestParam Double amount) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = accountService.transferFunds(fromAccountId, toAccountId, amount);
        return future.get().equals(1) ? ResponseEntity.ok("Transfer successful") : ResponseEntity.ok("Transfer failed");
    }
}
