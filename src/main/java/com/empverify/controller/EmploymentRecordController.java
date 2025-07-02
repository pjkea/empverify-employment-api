package com.empverify.controller;

import com.empverify.dto.*;
import com.empverify.service.EmploymentRecordService;
import com.empverify.exception.EmployeeRecordNotFoundException;
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

import java.util.Map;

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

    // ========================
    // EXISTING ENDPOINTS (Keep all current functionality)
    // ========================

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
    @Operation(summary = "Create Employment Record",
            description = "Create a new employment record with automatic duplicate detection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employment record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate record detected"),
            @ApiResponse(responseCode = "409", description = "Duplicate record exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> createEmploymentRecord(
            @Valid @RequestBody EmploymentRecordRequest request) {

        logger.info("Request to create employment record for employer: {}", request.getEmployerId());

        BlockchainResponse<String> response = employmentRecordService.createEmploymentRecord(request);

        // Handle duplicate detection responses
        if (!response.isSuccess() && response.getError() != null &&
                response.getError().contains("Duplicate record detected")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        return response.isSuccess() ?
                ResponseEntity.status(HttpStatus.CREATED).body(response) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Get Employment Record by Employee ID", description = "Retrieve an employment record by employee ID")
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
    @Operation(summary = "Update Employment Record by Employee ID",
            description = "Update an existing employment record with duplicate detection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employment record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "409", description = "Update would create duplicate record"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> updateEmploymentRecord(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Valid @RequestBody EmploymentRecordUpdateRequest request) {

        logger.info("Request to update employment record for employee ID: {}", employeeId);

        BlockchainResponse<String> response = employmentRecordService.updateEmploymentRecord(employeeId, request);

        // Handle duplicate detection responses
        if (!response.isSuccess() && response.getError() != null &&
                response.getError().contains("duplicate record detected")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/{employeeId}/documents/{documentType}")
    @Operation(summary = "Add Document by Employee ID", description = "Add a document to an employment record")
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
    @Operation(summary = "Get Documents by Employee ID", description = "Retrieve all documents for an employment record")
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
    @Operation(summary = "Get Employment Record History by Employee ID", description = "Retrieve the history of changes for an employment record")
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

    // ========================
    // NEW NATURAL IDENTIFIER ENDPOINTS
    // ========================

    @GetMapping("/by-identifiers")
    @Operation(summary = "Get Employment Record by Natural Identifiers",
            description = "Retrieve an employment record using National ID and Employer ID (user-friendly)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employment record retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "400", description = "Invalid identifiers"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<EmploymentRecordDto>> getEmploymentRecordByIdentifiers(
            @Parameter(description = "National ID (e.g., GHA-430120870-5)", required = true)
            @RequestParam String nationalId,

            @Parameter(description = "Employer ID", required = true)
            @RequestParam String employerId) {

        logger.info("Request to retrieve employment record by identifiers: nationalId='{}', employerId='{}'",
                maskNationalId(nationalId), employerId);

        try {
            // Resolve natural identifiers to employee ID
            String employeeId = employmentRecordService.resolveEmployeeId(nationalId, employerId);

            // Use existing endpoint logic
            return getEmploymentRecord(employeeId);

        } catch (EmployeeRecordNotFoundException e) {
            logger.warn("No employment record found for identifiers: nationalId='{}', employerId='{}'",
                    maskNationalId(nationalId), employerId);

            BlockchainResponse<EmploymentRecordDto> errorResponse =
                    BlockchainResponse.error("No employment record found for the provided identifiers");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            logger.error("Error retrieving employment record by identifiers", e);

            BlockchainResponse<EmploymentRecordDto> errorResponse =
                    BlockchainResponse.error("Failed to retrieve employment record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/by-identifiers")
    @Operation(summary = "Update Employment Record by Natural Identifiers",
            description = "Update an employment record using National ID and Employer ID (user-friendly)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employment record updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "400", description = "Invalid identifiers or request data"),
            @ApiResponse(responseCode = "409", description = "Update would create duplicate record"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> updateEmploymentRecordByIdentifiers(
            @Parameter(description = "National ID (e.g., GHA-430120870-5)", required = true)
            @RequestParam String nationalId,

            @Parameter(description = "Employer ID", required = true)
            @RequestParam String employerId,

            @Valid @RequestBody EmploymentRecordUpdateRequest request) {

        logger.info("Request to update employment record by identifiers: nationalId='{}', employerId='{}'",
                maskNationalId(nationalId), employerId);

        try {
            // Resolve natural identifiers to employee ID
            String employeeId = employmentRecordService.resolveEmployeeId(nationalId, employerId);

            // Use existing endpoint logic
            return updateEmploymentRecord(employeeId, request);

        } catch (EmployeeRecordNotFoundException e) {
            logger.warn("No employment record found for update: nationalId='{}', employerId='{}'",
                    maskNationalId(nationalId), employerId);

            BlockchainResponse<String> errorResponse =
                    BlockchainResponse.error("No employment record found for the provided identifiers");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            logger.error("Error updating employment record by identifiers", e);

            BlockchainResponse<String> errorResponse =
                    BlockchainResponse.error("Failed to update employment record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/by-identifiers/documents/{documentType}")
    @Operation(summary = "Add Document by Natural Identifiers",
            description = "Add a document to an employment record using National ID and Employer ID (user-friendly)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document added successfully"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "400", description = "Invalid identifiers or request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<String>> addDocumentByIdentifiers(
            @Parameter(description = "National ID (e.g., GHA-430120870-5)", required = true)
            @RequestParam String nationalId,

            @Parameter(description = "Employer ID", required = true)
            @RequestParam String employerId,

            @Parameter(description = "Document Type") @PathVariable String documentType,
            @Valid @RequestBody DocumentRequest documentRequest) {

        logger.info("Request to add document by identifiers: type='{}', nationalId='{}', employerId='{}'",
                documentType, maskNationalId(nationalId), employerId);

        try {
            // Resolve natural identifiers to employee ID
            String employeeId = employmentRecordService.resolveEmployeeId(nationalId, employerId);

            // Use existing endpoint logic
            return addDocument(employeeId, documentType, documentRequest);

        } catch (EmployeeRecordNotFoundException e) {
            logger.warn("No employment record found for document addition: nationalId='{}', employerId='{}'",
                    maskNationalId(nationalId), employerId);

            BlockchainResponse<String> errorResponse =
                    BlockchainResponse.error("No employment record found for the provided identifiers");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            logger.error("Error adding document by identifiers", e);

            BlockchainResponse<String> errorResponse =
                    BlockchainResponse.error("Failed to add document: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/by-identifiers/documents")
    @Operation(summary = "Get Documents by Natural Identifiers",
            description = "Retrieve all documents for an employment record using National ID and Employer ID (user-friendly)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "400", description = "Invalid identifiers"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<DocumentCollectionDto>> getDocumentsByIdentifiers(
            @Parameter(description = "National ID (e.g., GHA-430120870-5)", required = true)
            @RequestParam String nationalId,

            @Parameter(description = "Employer ID", required = true)
            @RequestParam String employerId) {

        logger.info("Request to retrieve documents by identifiers: nationalId='{}', employerId='{}'",
                maskNationalId(nationalId), employerId);

        try {
            // Resolve natural identifiers to employee ID
            String employeeId = employmentRecordService.resolveEmployeeId(nationalId, employerId);

            // Use existing endpoint logic
            return getDocuments(employeeId);

        } catch (EmployeeRecordNotFoundException e) {
            logger.warn("No employment record found for documents retrieval: nationalId='{}', employerId='{}'",
                    maskNationalId(nationalId), employerId);

            BlockchainResponse<DocumentCollectionDto> errorResponse =
                    BlockchainResponse.error("No employment record found for the provided identifiers");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            logger.error("Error retrieving documents by identifiers", e);

            BlockchainResponse<DocumentCollectionDto> errorResponse =
                    BlockchainResponse.error("Failed to retrieve documents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/by-identifiers/history")
    @Operation(summary = "Get Employment Record History by Natural Identifiers",
            description = "Retrieve the history of changes for an employment record using Natural ID and Employer ID (user-friendly)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employment record history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employment record not found"),
            @ApiResponse(responseCode = "400", description = "Invalid identifiers"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<EmploymentRecordHistoryDto>> getEmploymentRecordHistoryByIdentifiers(
            @Parameter(description = "National ID (e.g., GHA-430120870-5)", required = true)
            @RequestParam String nationalId,

            @Parameter(description = "Employer ID", required = true)
            @RequestParam String employerId) {

        logger.info("Request to retrieve employment record history by identifiers: nationalId='{}', employerId='{}'",
                maskNationalId(nationalId), employerId);

        try {
            // Resolve natural identifiers to employee ID
            String employeeId = employmentRecordService.resolveEmployeeId(nationalId, employerId);

            // Use existing endpoint logic
            return getEmploymentRecordHistory(employeeId);

        } catch (EmployeeRecordNotFoundException e) {
            logger.warn("No employment record found for history retrieval: nationalId='{}', employerId='{}'",
                    maskNationalId(nationalId), employerId);

            BlockchainResponse<EmploymentRecordHistoryDto> errorResponse =
                    BlockchainResponse.error("No employment record found for the provided identifiers");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            logger.error("Error retrieving employment record history by identifiers", e);

            BlockchainResponse<EmploymentRecordHistoryDto> errorResponse =
                    BlockchainResponse.error("Failed to retrieve employment record history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================
    // SMART CREATE/UPDATE ENDPOINT
    // ========================

    @PostMapping("/smart-upsert")
    @Operation(summary = "Smart Create/Update Employment Record",
            description = "Automatically create new record or update existing record based on National ID + Employer ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employment record updated successfully"),
            @ApiResponse(responseCode = "201", description = "Employment record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<EmploymentRecordUpsertResponse>> smartUpsertEmploymentRecord(
            @Valid @RequestBody EmploymentRecordRequest request) {

        logger.info("Smart upsert request for nationalId='{}', employerId='{}'",
                maskNationalId(request.getEmployeeName().getNationalId()), request.getEmployerId());

        try {
            EmploymentRecordUpsertResponse upsertResponse = employmentRecordService.smartUpsertEmploymentRecord(request);

            String message = upsertResponse.getWasUpdated() ?
                    "Employment record updated successfully" :
                    "Employment record created successfully";

            BlockchainResponse<EmploymentRecordUpsertResponse> response =
                    BlockchainResponse.success(message, upsertResponse);

            HttpStatus status = upsertResponse.getWasUpdated() ? HttpStatus.OK : HttpStatus.CREATED;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Error in smart upsert", e);

            BlockchainResponse<EmploymentRecordUpsertResponse> errorResponse =
                    BlockchainResponse.error("Failed to process employment record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================
    // EXISTING SYSTEM ENDPOINTS (Keep unchanged)
    // ========================

    @PostMapping("/check-duplicates")
    @Operation(summary = "Check for Duplicates",
            description = "Check if a potential employment record would be a duplicate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Duplicate check completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<DuplicateCheckDto>> checkForDuplicates(
            @Valid @RequestBody DuplicateCheckRequest request) {

        logger.info("Request to check for duplicates: employee='{}', employer='{}'",
                request.getEmployeeName().getFullName(), request.getEmployerId());

        BlockchainResponse<DuplicateCheckDto> response = employmentRecordService.checkForDuplicates(request);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/system/duplicate-prevention")
    @Operation(summary = "Get Duplicate Prevention Configuration",
            description = "Retrieve the current duplicate prevention settings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<Map<String, Object>>> getDuplicatePreventionConfig() {
        logger.info("Request to retrieve duplicate prevention configuration");

        BlockchainResponse<Map<String, Object>> response = employmentRecordService.getDuplicatePreventionConfig();

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

    // ========================
    // UTILITY METHODS
    // ========================

    /**
     * Mask National ID for logging (show only last 4 characters)
     */
    private String maskNationalId(String nationalId) {
        if (nationalId == null || nationalId.length() < 4) {
            return "****";
        }
        return "****" + nationalId.substring(nationalId.length() - 4);
    }
}