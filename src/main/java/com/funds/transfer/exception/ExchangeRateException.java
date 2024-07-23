package com.funds.transfer.exception;

public class ExchangeRateException extends RuntimeException {
    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }
}