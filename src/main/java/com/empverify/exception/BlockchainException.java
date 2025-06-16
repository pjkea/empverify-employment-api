package com.empverify.exception;

// BlockchainException
public class BlockchainException extends RuntimeException {
    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}