package com.empverify.controller;

import com.empverify.dto.*;
import com.empverify.service.EmploymentRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employment-records")
@Tag(name = "Employment Records", description = "Employment Record Management API")
@SecurityRequirement(name = "apiKey")
public class EmploymentRecordController {

    private static final Logger logger = LoggerFactory.getLogger(EmploymentRecordController.class);

    private final EmploymentRecordService employmentRecordService;

    @Autowired
    public EmploymentRecordController(EmploymentRecordService employmentRecordService) {
        this.employmentRecordService = employmentRecordService;
    }

    @PostMapping("/init")
    @Operation(summary = "Initialize Ledger", description = "Initialize the employment record ledger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ledger initialized successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> initializeLedger() {
        logger.info("Request to initialize employment record ledger");

        BlockchainResponse<String> response = employmentRecordService.initializeLedger();

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @PostMapping
    @Operation(summary = "Create Employment Record", description = "Create a new employment record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employment record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> createEmploymentRecord(
            @Valid @RequestBody EmploymentRecordRequest request) {

        logger.info("Request to create employment record for employer: {}", request.getEmployerId());

        BlockchainResponse<String> response = employmentRecordService.createEmploymentRecord(request);

        return response.isSuccess() ?
                ResponseEntity.status(HttpStatus.CREATED).body(response) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Get Employment Record", description = "Retrieve an employment record by employee ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employment record retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<EmploymentRecordDto>> getEmploymentRecord(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {

        logger.info("Request to retrieve employment record for employee ID: {}", employeeId);

        try {
            BlockchainResponse<EmploymentRecordDto> response = employmentRecordService.getEmploymentRecord(employeeId);

            return response.isSuccess() ?
                    ResponseEntity.ok(response) :
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            logger.error("Error retrieving employment record for employee ID: {}", employeeId, e);

            if (e.getMessage().contains("not found")) {
                BlockchainResponse<EmploymentRecordDto> errorResponse =
                        BlockchainResponse.error("Employment record not found for employee ID: " + employeeId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            BlockchainResponse<EmploymentRecordDto> errorResponse =
                    BlockchainResponse.error("Failed to retrieve employment record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{employeeId}")
    @Operation(summary = "Update Employment Record", description = "Update an existing employment record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employment record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> updateEmploymentRecord(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Valid @RequestBody EmploymentRecordUpdateRequest request) {

        logger.info("Request to update employment record for employee ID: {}", employeeId);

        BlockchainResponse<String> response = employmentRecordService.updateEmploymentRecord(employeeId, request);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/{employeeId}/documents/{documentType}")
    @Operation(summary = "Add Document", description = "Add a document to an employment record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> addDocument(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Document Type") @PathVariable String documentType,
            @Valid @RequestBody DocumentRequest documentRequest) {

        logger.info("Request to add document of type {} for employee ID: {}", documentType, employeeId);

        BlockchainResponse<String> response = employmentRecordService.addDocument(employeeId, documentType, documentRequest);

        return response.isSuccess() ?
                ResponseEntity.status(HttpStatus.CREATED).body(response) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/{employeeId}/documents")
    @Operation(summary = "Get Documents", description = "Retrieve all documents for an employment record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<DocumentCollectionDto>> getDocuments(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {

        logger.info("Request to retrieve documents for employee ID: {}", employeeId);

        BlockchainResponse<DocumentCollectionDto> response = employmentRecordService.getDocuments(employeeId);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/{employeeId}/history")
    @Operation(summary = "Get Employment Record History", description = "Retrieve the history of changes for an employment record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employment record history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<EmploymentRecordHistoryDto>> getEmploymentRecordHistory(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {

        logger.info("Request to retrieve employment record history for employee ID: {}", employeeId);

        BlockchainResponse<EmploymentRecordHistoryDto> response = employmentRecordService.getEmploymentRecordHistory(employeeId);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/system/info")
    @Operation(summary = "Get System Info", description = "Retrieve system information and configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System information retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<SystemInfoDto>> getSystemInfo() {
        logger.info("Request to retrieve system information");

        BlockchainResponse<SystemInfoDto> response = employmentRecordService.getSystemInfo();

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/system/counter")
    @Operation(summary = "Get Employee Counter", description = "Retrieve the current employee counter for a given year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee counter retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<EmployeeCounterDto>> getEmployeeCounter(
            @Parameter(description = "Year (optional, defaults to current year)")
            @RequestParam(required = false) Integer year) {

        logger.info("Request to retrieve employee counter for year: {}", year);

        BlockchainResponse<EmployeeCounterDto> response = employmentRecordService.getEmployeeCounter(year);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check the health of the blockchain connection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy"),
            @ApiResponse(responseCode = "503", description = "Service is unhealthy")
    })
    public ResponseEntity<HealthCheckDto> healthCheck() {
        logger.debug("Health check request");

        boolean isConnected = employmentRecordService.isBlockchainConnected();
        HealthCheckDto healthCheck = new HealthCheckDto(
                isConnected ? "UP" : "DOWN",
                isConnected ? "Blockchain connection is healthy" : "Blockchain connection is down"
        );

        return isConnected ?
                ResponseEntity.ok(healthCheck) :
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthCheck);
    }
}