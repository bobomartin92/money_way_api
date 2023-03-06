package com.example.money_way.exception;

public class InsufficientFundsException extends RuntimeException{
    private String message;

    public InsufficientFundsException(String message) {
        this.message = message;
    }
}
