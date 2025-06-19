package com.empverify.service;

import com.empverify.dto.*;
import com.empverify.exception.BlockchainException;
import com.empverify.exception.EmployeeRecordNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmploymentRecordService {

    private static final Logger logger = LoggerFactory.getLogger(EmploymentRecordService.class);

    private final FabricGatewayService fabricGatewayService;
    private final ObjectMapper objectMapper;
    private final DuplicatePreventionService duplicatePreventionService;

    @Autowired
    public EmploymentRecordService(FabricGatewayService fabricGatewayService,
                                   ObjectMapper objectMapper,
                                   DuplicatePreventionService duplicatePreventionService) {
        this.fabricGatewayService = fabricGatewayService;
        this.objectMapper = objectMapper;
        this.duplicatePreventionService = duplicatePreventionService;
    }

    public BlockchainResponse<String> initializeLedger() {
        try {
            logger.info("Initializing employment record ledger");
            String result = fabricGatewayService.submitTransaction("initLedger");

            return BlockchainResponse.success("Ledger initialized successfully", result);
        } catch (Exception e) {
            logger.error("Failed to initialize ledger", e);
            return BlockchainResponse.error("Failed to initialize ledger: " + e.getMessage());
        }
    }

    public BlockchainResponse<String> createEmploymentRecord(EmploymentRecordRequest request) {
        try {
            logger.info("Creating employment record for employer: {}", request.getEmployerId());

            // Check for duplicates BEFORE creating the record
            DuplicateCheckDto duplicateCheck = duplicatePreventionService.checkForDuplicates(
                    request.getEmployeeName(),
                    request.getEmployerId()
            );

            // Block creation if duplicate is found and should be blocked
            if (duplicatePreventionService.shouldBlockDuplicate(duplicateCheck)) {
                String errorMessage = String.format(
                        "Duplicate record detected: %s. Employee already exists for this employer.",
                        duplicateCheck.getMessage()
                );

                logger.warn("Blocking duplicate record creation: {}", errorMessage);
                return BlockchainResponse.error(errorMessage);
            }

            // Log warning for similar matches that aren't blocked
            if (duplicateCheck.getIsDuplicate() && !duplicatePreventionService.shouldBlockDuplicate(duplicateCheck)) {
                logger.warn("Similar record detected but allowing creation: {} - {}",
                        duplicateCheck.getMessage(), duplicateCheck.getExistingEmployeeIds());
            }

            // Proceed with record creation
            String recordJson = objectMapper.writeValueAsString(request);
            String result = fabricGatewayService.submitTransaction("createRecord", recordJson);

            // Parse the result to extract employee ID
            EmploymentRecordResponse response = objectMapper.readValue(result, EmploymentRecordResponse.class);

            logger.info("Successfully created employment record with ID: {}", response.getEmployeeId());

            // Include duplicate check info in success response if there were warnings
            String successMessage = "Employment record created successfully";
            if (duplicateCheck.getIsDuplicate()) {
                successMessage += " (Warning: " + duplicateCheck.getMessage() + ")";
            }

            return BlockchainResponse.success(successMessage, result);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize employment record request", e);
            return BlockchainResponse.error("Invalid request format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create employment record", e);
            return BlockchainResponse.error("Failed to create employment record: " + e.getMessage());
        }
    }

    public BlockchainResponse<EmploymentRecordDto> getEmploymentRecord(String employeeId) {
        try {
            logger.info("Retrieving employment record for employee ID: {}", employeeId);

            String result = fabricGatewayService.evaluateTransaction("getRecord", employeeId);
            EmploymentRecordDto record = objectMapper.readValue(result, EmploymentRecordDto.class);

            logger.info("Successfully retrieved employment record for employee ID: {}", employeeId);
            return BlockchainResponse.success("Employment record retrieved successfully", record);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse employment record response", e);
            return BlockchainResponse.error("Failed to parse response: " + e.getMessage());
        } catch (BlockchainException e) {
            if (e.getMessage().contains("RECORD_NOT_FOUND")) {
                throw new EmployeeRecordNotFoundException("Employment record not found for employee ID: " + employeeId);
            }
            logger.error("Blockchain error retrieving employment record", e);
            return BlockchainResponse.error("Blockchain error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to retrieve employment record for employee ID: {}", employeeId, e);
            return BlockchainResponse.error("Failed to retrieve employment record: " + e.getMessage());
        }
    }

    public BlockchainResponse<String> updateEmploymentRecord(String employeeId, EmploymentRecordUpdateRequest request) {
        try {
            logger.info("Updating employment record for employee ID: {}", employeeId);

            // Set the employee ID in the request
            request.setEmployeeId(employeeId);

            String recordJson = objectMapper.writeValueAsString(request);
            String result = fabricGatewayService.submitTransaction("updateRecord", recordJson);

            logger.info("Successfully updated employment record for employee ID: {}", employeeId);
            return BlockchainResponse.success("Employment record updated successfully", result);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize employment record update request", e);
            return BlockchainResponse.error("Invalid request format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update employment record for employee ID: {}", employeeId, e);
            return BlockchainResponse.error("Failed to update employment record: " + e.getMessage());
        }
    }

    public BlockchainResponse<String> addDocument(String employeeId, String documentType, DocumentRequest documentRequest) {
        try {
            logger.info("Adding document of type {} for employee ID: {}", documentType, employeeId);

            String documentJson = objectMapper.writeValueAsString(documentRequest);
            String result = fabricGatewayService.submitTransaction("addDocument", employeeId, documentType, documentJson);

            logger.info("Successfully added document for employee ID: {}", employeeId);
            return BlockchainResponse.success("Document added successfully", result);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize document request", e);
            return BlockchainResponse.error("Invalid document format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to add document for employee ID: {}", employeeId, e);
            return BlockchainResponse.error("Failed to add document: " + e.getMessage());
        }
    }

    public BlockchainResponse<DocumentCollectionDto> getDocuments(String employeeId) {
        try {
            logger.info("Retrieving documents for employee ID: {}", employeeId);

            String result = fabricGatewayService.evaluateTransaction("getDocuments", employeeId);
            DocumentCollectionDto documents = objectMapper.readValue(result, DocumentCollectionDto.class);

            logger.info("Successfully retrieved documents for employee ID: {}", employeeId);
            return BlockchainResponse.success("Documents retrieved successfully", documents);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse documents response", e);
            return BlockchainResponse.error("Failed to parse response: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to retrieve documents for employee ID: {}", employeeId, e);
            return BlockchainResponse.error("Failed to retrieve documents: " + e.getMessage());
        }
    }

    public BlockchainResponse<EmploymentRecordHistoryDto> getEmploymentRecordHistory(String employeeId) {
        try {
            logger.info("Retrieving employment record history for employee ID: {}", employeeId);

            String result = fabricGatewayService.evaluateTransaction("getRecordHistory", employeeId);
            EmploymentRecordHistoryDto history = objectMapper.readValue(result, EmploymentRecordHistoryDto.class);

            logger.info("Successfully retrieved employment record history for employee ID: {}", employeeId);
            return BlockchainResponse.success("Employment record history retrieved successfully", history);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse employment record history response", e);
            return BlockchainResponse.error("Failed to parse response: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to retrieve employment record history for employee ID: {}", employeeId, e);
            return BlockchainResponse.error("Failed to retrieve employment record history: " + e.getMessage());
        }
    }

    public BlockchainResponse<SystemInfoDto> getSystemInfo() {
        try {
            logger.info("Retrieving system information");

            String result = fabricGatewayService.evaluateTransaction("getSystemInfo");
            SystemInfoDto systemInfo = objectMapper.readValue(result, SystemInfoDto.class);

            logger.info("Successfully retrieved system information");
            return BlockchainResponse.success("System information retrieved successfully", systemInfo);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse system info response", e);
            return BlockchainResponse.error("Failed to parse response: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to retrieve system information", e);
            return BlockchainResponse.error("Failed to retrieve system information: " + e.getMessage());
        }
    }

    public BlockchainResponse<EmployeeCounterDto> getEmployeeCounter(Integer year) {
        try {
            logger.info("Retrieving employee counter for year: {}", year);

            String result;
            if (year != null) {
                result = fabricGatewayService.evaluateTransaction("getEmployeeCounter", year.toString());
            } else {
                result = fabricGatewayService.evaluateTransaction("getEmployeeCounter");
            }

            EmployeeCounterDto counter = objectMapper.readValue(result, EmployeeCounterDto.class);

            logger.info("Successfully retrieved employee counter");
            return BlockchainResponse.success("Employee counter retrieved successfully", counter);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse employee counter response", e);
            return BlockchainResponse.error("Failed to parse response: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to retrieve employee counter", e);
            return BlockchainResponse.error("Failed to retrieve employee counter: " + e.getMessage());
        }
    }

    public boolean isBlockchainConnected() {
        return fabricGatewayService.isConnected();
    }

    /**
     * Check for duplicates - exposed as a service method for testing/debugging
     */
    public BlockchainResponse<DuplicateCheckDto> checkForDuplicates(DuplicateCheckRequest request) {
        try {
            logger.info("Checking for duplicates: employee='{}', employer='{}'",
                    request.getEmployeeName().getFullName(), request.getEmployerId());

            DuplicateCheckDto duplicateCheck = duplicatePreventionService.checkForDuplicates(request);

            logger.info("Duplicate check completed: isDuplicate={}, message={}",
                    duplicateCheck.getIsDuplicate(), duplicateCheck.getMessage());

            return BlockchainResponse.success("Duplicate check completed", duplicateCheck);

        } catch (Exception e) {
            logger.error("Failed to check for duplicates", e);
            return BlockchainResponse.error("Failed to check for duplicates: " + e.getMessage());
        }
    }

    /**
     * Get duplicate prevention configuration
     */
    public BlockchainResponse<Map<String, Object>> getDuplicatePreventionConfig() {
        try {
            Map<String, Object> config = duplicatePreventionService.getConfigurationInfo();
            return BlockchainResponse.success("Duplicate prevention configuration retrieved", config);
        } catch (Exception e) {
            logger.error("Failed to get duplicate prevention configuration", e);
            return BlockchainResponse.error("Failed to get configuration: " + e.getMessage());
        }
    }
}