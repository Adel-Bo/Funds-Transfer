package com.funds.transfer.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.funds.transfer.exception.ExchangeRateException;
import com.funds.transfer.exception.WrongCurrencyFormatException;
import com.funds.transfer.model.ExchangeRate;
import com.funds.transfer.model.ExchangeRateResponse;
import com.funds.transfer.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceImplTests {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateServiceImpl exchangeRateService;

    private final String apiUrl = "https://open.er-api.com/v6/latest";

    @BeforeEach
    public void setUp() {
        exchangeRateService.apiUrl = apiUrl;
    }

    @Test
    public void testSaveExchangeRates() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("EUR", 0.85);

        exchangeRateService.saveExchangeRates(rates);

        verify(exchangeRateRepository, times(2)).save(any(ExchangeRate.class));
    }

    @Test
    public void testGetExchangeRates() {
        ExchangeRateResponse response = new ExchangeRateResponse();
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("EUR", 0.85);
        response.setRates(rates);

        when(restTemplate.getForObject(apiUrl, ExchangeRateResponse.class)).thenReturn(response);

        Map<String, Double> result = exchangeRateService.getExchangeRates();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1.0, result.get("USD"));
        assertEquals(0.85, result.get("EUR"));
    }

    @Test
    public void testGetExchangeRate() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("EUR", 0.85);
        ExchangeRateResponse response = new ExchangeRateResponse();
        response.setRates(rates);

        when(restTemplate.getForObject(apiUrl + "/USD", ExchangeRateResponse.class)).thenReturn(response);
        //when(exchangeRateService.getCurrencyExchangeRates("USD")).thenReturn(rates);

        Double rate = exchangeRateService.getExchangeRate("USD", "EUR");

        assertNotNull(rate);
        assertEquals(0.85, rate);
    }

    @Test
    public void testGetExchangeRateByCurrency() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setCurrency("USD");
        exchangeRate.setRate(1.0);

        when(exchangeRateRepository.findByCurrency("USD")).thenReturn(exchangeRate);

        ExchangeRate result = exchangeRateService.getExchangeRateByCurrency("USD");

        assertNotNull(result);
        assertEquals("USD", result.getCurrency());
        assertEquals(1.0, result.getRate());
    }

    @Test
    public void testGetCurrencyExchangeRates() {
        String currency = "USD";
        String url = String.format("%s/%s", apiUrl, currency);

        ExchangeRateResponse response = new ExchangeRateResponse();
        Map<String, Double> rates = new HashMap<>();
        rates.put("EUR", 0.85);
        response.setRates(rates);

        when(restTemplate.getForObject(url, ExchangeRateResponse.class)).thenReturn(response);

        Map<String, Double> result = exchangeRateService.getCurrencyExchangeRates(currency);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0.85, result.get("EUR"));
    }

    @Test
    public void testGetExchangeRate_SameCurrency() {
        Double rate = exchangeRateService.getExchangeRate("USD", "USD");

        assertNotNull(rate);
        assertEquals(1.0, rate);
    }

    @Test
    public void testGetExchangeRate_WrongCurrencyFormatException() {
        when(restTemplate.getForObject(apiUrl + "/INVALID", ExchangeRateResponse.class)).thenReturn(new ExchangeRateResponse());

        assertThrows(WrongCurrencyFormatException.class, () -> {
            exchangeRateService.getExchangeRate("INVALID", "EUR");
        });
    }

    @Test
    public void testGetCurrencyExchangeRates_ExchangeRateException() {
        String currency = "USD";
        String url = String.format("%s/%s", apiUrl, currency);

        when(restTemplate.getForObject(url, ExchangeRateResponse.class)).thenThrow(new RuntimeException("API error"));

        assertThrows(ExchangeRateException.class, () -> {
            exchangeRateService.getCurrencyExchangeRates(currency);
        });
    }
}
