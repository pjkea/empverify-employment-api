package com.empverify.exception;

import com.empverify.dto.BlockchainResponse;
import com.empverify.dto.DuplicateCheckDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmployeeRecordNotFoundException.class)
    public ResponseEntity<BlockchainResponse<String>> handleEmployeeRecordNotFoundException(
            EmployeeRecordNotFoundException ex) {
        logger.warn("Employee record not found: {}", ex.getMessage());

        BlockchainResponse<String> response = BlockchainResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateRecordException.class)
    public ResponseEntity<BlockchainResponse<DuplicateCheckDto>> handleDuplicateRecordException(
            DuplicateRecordException ex) {
        logger.warn("Duplicate record detected: {}", ex.getMessage());

        BlockchainResponse<DuplicateCheckDto> response = BlockchainResponse.error(ex.getMessage());
        response.setData(ex.getDuplicateCheck());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BlockchainException.class)
    public ResponseEntity<BlockchainResponse<String>> handleBlockchainException(BlockchainException ex) {
        logger.error("Blockchain operation failed: {}", ex.getMessage(), ex);

        BlockchainResponse<String> response = BlockchainResponse.error("Blockchain operation failed: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BlockchainResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Validation failed: {}", errors);

        BlockchainResponse<Map<String, String>> response = BlockchainResponse.error("Validation failed");
        response.setData(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BlockchainResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());

        BlockchainResponse<String> response = BlockchainResponse.error("Invalid argument: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BlockchainResponse<String>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        BlockchainResponse<String> response = BlockchainResponse.error("An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}