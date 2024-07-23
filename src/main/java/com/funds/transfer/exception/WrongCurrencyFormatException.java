package com.funds.transfer.exception;

public class WrongCurrencyFormatException extends RuntimeException {
    public WrongCurrencyFormatException(String message) {
        super(message);
    }
}



