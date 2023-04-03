package com.mihak.jumun.menu.exception;

public class StockLockTakeFailedException extends RuntimeException {
    public StockLockTakeFailedException(String message) {
        super(message);
    }
}
