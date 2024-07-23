package com.funds.transfer.service.scheduler;

import com.funds.transfer.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExchangeRateUpdater {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Scheduled(fixedRate = 3600000) // Update every hour
    public void updateExchangeRates() {
        Map<String, Double> rates = exchangeRateService.getExchangeRates();
        if (rates != null) {
            exchangeRateService.saveExchangeRates(rates);
        }
    }
}
