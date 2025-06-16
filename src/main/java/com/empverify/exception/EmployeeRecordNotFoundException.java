package com.empverify.exception;

public class EmployeeRecordNotFoundException extends RuntimeException {
    public EmployeeRecordNotFoundException(String message) {
        super(message);
    }

    public EmployeeRecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}