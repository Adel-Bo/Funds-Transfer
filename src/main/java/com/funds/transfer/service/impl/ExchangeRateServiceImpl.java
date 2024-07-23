package com.funds.transfer.service.impl;

import com.funds.transfer.exception.ExchangeRateException;
import com.funds.transfer.exception.WrongCurrencyFormatException;
import com.funds.transfer.model.ExchangeRate;
import com.funds.transfer.model.ExchangeRateResponse;
import com.funds.transfer.repository.ExchangeRateRepository;
import com.funds.transfer.service.ExchangeRateService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    @Value("${exchange.api.url}")
    String apiUrl;

    @Value("${exchange.api.key}")
    String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Transactional
    public void saveExchangeRates(Map<String, Double> rates) {
        rates.forEach((currency, rate) -> {
            ExchangeRate exchangeRate = new ExchangeRate();
            exchangeRate.setCurrency(currency);
            exchangeRate.setRate(rate);
            exchangeRateRepository.save(exchangeRate);
        });
    }

    @Override
    public Map<String, Double> getExchangeRates() {
        //String url = String.format("%s?access_key=%s", apiUrl, apiKey);
        ExchangeRateResponse response = restTemplate.getForObject(apiUrl, ExchangeRateResponse.class);
        return response != null ? response.getRates() : null;
    }

    @Override
    public Double getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }
        Double exchangeRate = getCurrencyExchangeRates(fromCurrency).get(toCurrency);
        if (exchangeRate != null) {
            return exchangeRate;
        }
        throw new WrongCurrencyFormatException("Wrong credit account currency code: " + toCurrency);
    }

    @Override
    public ExchangeRate getExchangeRateByCurrency(String currency) {
        return exchangeRateRepository.findByCurrency(currency);
    }

    @Override
    public Map<String, Double> getCurrencyExchangeRates(String currency) {
        String url = String.format("%s/%s", apiUrl, currency);
        try {
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
            if (response != null && response.getRates() != null) {
                return response.getRates();
            }
        } catch (Exception e) {
            throw new ExchangeRateException("Failed to retrieve exchange rate", e);
        }
        throw new WrongCurrencyFormatException("Wrong debit account currency code: " + currency);
    }
}

