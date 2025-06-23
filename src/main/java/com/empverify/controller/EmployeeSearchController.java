package com.empverify.controller;

import com.empverify.dto.*;
import com.empverify.service.EmployeeSearchService;
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
@RequestMapping("/employment-records/search")
@Tag(name = "Employee Search", description = "Search and lookup ex-employee records")
@SecurityRequirement(name = "apiKey")
public class EmployeeSearchController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeSearchController.class);

    private final EmployeeSearchService employeeSearchService;

    @Autowired
    public EmployeeSearchController(EmployeeSearchService employeeSearchService) {
        this.employeeSearchService = employeeSearchService;
    }

    @PostMapping
    @Operation(summary = "Advanced Employee Search",
            description = "Search ex-employee records using multiple criteria with disambiguation support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<SearchResponse>> searchEmployees(
            @Valid @RequestBody SearchRequest searchRequest) {

        logger.info("Advanced search request: name='{}', employer='{}', searchType='{}'",
                searchRequest.getEmployeeName(), searchRequest.getEmployerId(), searchRequest.getSearchType());

        try {
            SearchResponse searchResponse = employeeSearchService.searchEmployees(searchRequest);

            String message = String.format("Search completed: %d result(s) found", searchResponse.getTotalResults());
            BlockchainResponse<SearchResponse> response = BlockchainResponse.success(message, searchResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during employee search", e);
            BlockchainResponse<SearchResponse> errorResponse =
                    BlockchainResponse.error("Search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/by-name")
    @Operation(summary = "Search by Employee Name",
            description = "Quick search by employee name with optional employer filter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<SearchResponse>> searchByName(
            @Parameter(description = "Employee name (partial match supported)")
            @RequestParam String name,

            @Parameter(description = "Employer ID (optional filter)")
            @RequestParam(required = false) String employerId,

            @Parameter(description = "Search type: exact, partial, fuzzy")
            @RequestParam(defaultValue = "partial") String searchType,

            @Parameter(description = "Maximum number of results")
            @RequestParam(defaultValue = "10") Integer maxResults) {

        logger.info("Search by name: '{}', employer: '{}', type: '{}'", name, employerId, searchType);

        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setEmployeeName(name);
            searchRequest.setEmployerId(employerId);
            searchRequest.setSearchType(searchType);
            searchRequest.setMaxResults(maxResults);

            SearchResponse searchResponse = employeeSearchService.searchEmployees(searchRequest);

            String message = String.format("Name search completed: %d result(s) found", searchResponse.getTotalResults());
            BlockchainResponse<SearchResponse> response = BlockchainResponse.success(message, searchResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during name search", e);
            BlockchainResponse<SearchResponse> errorResponse =
                    BlockchainResponse.error("Name search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/by-employer")
    @Operation(summary = "Search by Employer",
            description = "Get all ex-employees for a specific employer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid employer ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<SearchResponse>> searchByEmployer(
            @Parameter(description = "Employer ID", required = true)
            @RequestParam String employerId,

            @Parameter(description = "Maximum number of results")
            @RequestParam(defaultValue = "20") Integer maxResults) {

        logger.info("Search by employer: '{}', maxResults: {}", employerId, maxResults);

        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setEmployerId(employerId);
            searchRequest.setMaxResults(maxResults);

            SearchResponse searchResponse = employeeSearchService.searchEmployees(searchRequest);

            String message = String.format("Employer search completed: %d ex-employee(s) found", searchResponse.getTotalResults());
            BlockchainResponse<SearchResponse> response = BlockchainResponse.success(message, searchResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during employer search", e);
            BlockchainResponse<SearchResponse> errorResponse =
                    BlockchainResponse.error("Employer search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/by-composite-key")
    @Operation(summary = "Search by Composite Key",
            description = "Precise search using name + employer + employment period")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<SearchResponse>> searchByCompositeKey(
            @Parameter(description = "Employee name", required = true)
            @RequestParam String name,

            @Parameter(description = "Employer ID", required = true)
            @RequestParam String employerId,

            @Parameter(description = "Employment start date (YYYY-MM-DD)", required = true)
            @RequestParam String startDate,

            @Parameter(description = "Employment end date (YYYY-MM-DD)")
            @RequestParam(required = false) String endDate) {

        logger.info("Composite key search: name='{}', employer='{}', period='{} to {}'",
                name, employerId, startDate, endDate);

        try {
            SearchRequest searchRequest = SearchRequest.byCompositeKey(name, employerId, startDate, endDate);

            SearchResponse searchResponse = employeeSearchService.searchEmployees(searchRequest);

            String message = String.format("Composite key search completed: %d result(s) found", searchResponse.getTotalResults());
            BlockchainResponse<SearchResponse> response = BlockchainResponse.success(message, searchResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during composite key search", e);
            BlockchainResponse<SearchResponse> errorResponse =
                    BlockchainResponse.error("Composite key search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/by-national-id")
    @Operation(summary = "Search by National ID",
            description = "Most precise search using national ID/SSN + employer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<SearchResponse>> searchByNationalId(
            @Parameter(description = "National ID/SSN (last 4 digits acceptable)", required = true)
            @RequestParam String nationalId,

            @Parameter(description = "Employer ID", required = true)
            @RequestParam String employerId) {

        logger.info("National ID search: nationalId='****{}', employer='{}'",
                nationalId.length() >= 4 ? nationalId.substring(nationalId.length() - 4) : "****", employerId);

        try {
            SearchRequest searchRequest = SearchRequest.byNationalIdAndEmployer(nationalId, employerId);

            SearchResponse searchResponse = employeeSearchService.searchEmployees(searchRequest);

            String message = String.format("National ID search completed: %d result(s) found", searchResponse.getTotalResults());
            BlockchainResponse<SearchResponse> response = BlockchainResponse.success(message, searchResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during national ID search", e);
            BlockchainResponse<SearchResponse> errorResponse =
                    BlockchainResponse.error("National ID search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/verification")
    @Operation(summary = "Employment Verification",
            description = "Verify employment history for background checks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification completed"),
            @ApiResponse(responseCode = "404", description = "No employment record found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BlockchainResponse<SearchResponse>> verifyEmployment(
            @Parameter(description = "Employee name", required = true)
            @RequestParam String name,

            @Parameter(description = "Employer name or ID", required = true)
            @RequestParam String employer,

            @Parameter(description = "Expected employment start date (YYYY-MM)")
            @RequestParam(required = false) String expectedStartDate,

            @Parameter(description = "Expected employment end date (YYYY-MM)")
            @RequestParam(required = false) String expectedEndDate) {

        logger.info("Employment verification: name='{}', employer='{}', period='{} to {}'",
                name, employer, expectedStartDate, expectedEndDate);

        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setEmployeeName(name);

            // Try employer ID first, then employer name
            if (employer.length() <= 10 && employer.toUpperCase().equals(employer)) {
                searchRequest.setEmployerId(employer);
            } else {
                searchRequest.setEmployerName(employer);
            }

            searchRequest.setEmploymentStartDate(expectedStartDate);
            searchRequest.setEmploymentEndDate(expectedEndDate);
            searchRequest.setSearchType("exact");
            searchRequest.setMaxResults(5);

            SearchResponse searchResponse = employeeSearchService.searchEmployees(searchRequest);

            if (searchResponse.getTotalResults() == 0) {
                BlockchainResponse<SearchResponse> response =
                        BlockchainResponse.success("No employment record found for verification", searchResponse);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String message = String.format("Employment verification completed: %d matching record(s)",
                    searchResponse.getTotalResults());
            BlockchainResponse<SearchResponse> response = BlockchainResponse.success(message, searchResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during employment verification", e);
            BlockchainResponse<SearchResponse> errorResponse =
                    BlockchainResponse.error("Employment verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}