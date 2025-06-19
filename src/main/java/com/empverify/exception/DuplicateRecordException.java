package com.empverify.exception;

import com.empverify.dto.DuplicateCheckDto;

public class DuplicateRecordException extends RuntimeException {

    private final DuplicateCheckDto duplicateCheck;

    public DuplicateRecordException(String message, DuplicateCheckDto duplicateCheck) {
        super(message);
        this.duplicateCheck = duplicateCheck;
    }

    public DuplicateRecordException(String message, DuplicateCheckDto duplicateCheck, Throwable cause) {
        super(message, cause);
        this.duplicateCheck = duplicateCheck;
    }

    public DuplicateCheckDto getDuplicateCheck() {
        return duplicateCheck;
    }
}