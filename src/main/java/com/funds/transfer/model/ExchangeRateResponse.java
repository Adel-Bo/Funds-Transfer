package com.funds.transfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ExchangeRateResponse {
    @JsonProperty("rates")
    private Map<String, Double> rates;
}
