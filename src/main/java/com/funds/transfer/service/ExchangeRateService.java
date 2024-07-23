package com.funds.transfer.service;

import com.funds.transfer.model.ExchangeRate;

import java.util.Map;

public interface ExchangeRateService {

    Double getExchangeRate(String fromCurrency, String toCurrency);
    ExchangeRate getExchangeRateByCurrency(String currency);
    Map<String, Double> getExchangeRates();
    void saveExchangeRates(Map<String, Double> rates);
    Map<String, Double> getCurrencyExchangeRates(String currency);
}
