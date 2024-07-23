package com.funds.transfer.controller;

import com.funds.transfer.model.ExchangeRate;
import com.funds.transfer.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exchange-rate")
public class ExchangeRateController {
    @Autowired
    private ExchangeRateService exchangeRateService;

    @GetMapping
    public Double getExchangeRate(@RequestParam String currency) {
        ExchangeRate exchangeRate = exchangeRateService.getExchangeRateByCurrency(currency);
        return exchangeRate != null ? exchangeRate.getRate() : null;
    }
}
