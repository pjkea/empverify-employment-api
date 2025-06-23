package com.empverify.service;

import com.empverify.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeSearchService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeSearchService.class);
    private static final double FUZZY_MATCH_THRESHOLD = 0.7;

    private final FabricGatewayService fabricGatewayService;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmployeeSearchService(FabricGatewayService fabricGatewayService, ObjectMapper objectMapper) {
        this.fabricGatewayService = fabricGatewayService;
        this.objectMapper = objectMapper;
    }

    // ==================== PUBLIC API ====================

    /**
     * Main search method - handles all search types
     */
    public SearchResponse searchEmployees(SearchRequest searchRequest) {
        long startTime = System.currentTimeMillis();

        try {
            logger.info("Searching employees with criteria: name='{}', employer='{}'",
                    searchRequest.getEmployeeName(), searchRequest.getEmployerId());

            SearchResponse response = executeSearch(searchRequest);

            response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            response.setSearchQuery(searchRequest);

            logger.info("Search completed: {} results found in {} ms",
                    response.getTotalResults(), response.getExecutionTimeMs());

            return response;

        } catch (Exception e) {
            logger.error("Error during employee search", e);
            SearchResponse errorResponse = SearchResponse.noResults(searchRequest);
            errorResponse.addSearchTip("Search failed: " + e.getMessage());
            return errorResponse;
        }
    }

    // ==================== SEARCH STRATEGY EXECUTION ====================

    /**
     * Execute search based on available criteria - determines optimal search strategy
     */
    private SearchResponse executeSearch(SearchRequest request) {
        // Strategy 1: National ID + Employer (highest precision)
        if (hasValue(request.getNationalId()) && hasValue(request.getEmployerId())) {
            return searchByNationalIdAndEmployer(request);
        }

        // Strategy 2: Composite key (Name + Employer + Dates) - most precise for name-based searches
        if (hasValue(request.getEmployeeName()) && hasValue(request.getEmployerId())
                && hasValue(request.getEmploymentStartDate())) {
            return searchByCompositeKey(request);
        }

        // Strategy 3: Name + Employer (common case)
        if (hasValue(request.getEmployeeName()) && hasValue(request.getEmployerId())) {
            return searchByNameAndEmployer(request);
        }

        // Strategy 4: Name only (broadest search)
        if (hasValue(request.getEmployeeName())) {
            return searchByNameOnly(request);
        }

        // Strategy 5: Employer only
        if (hasValue(request.getEmployerId())) {
            return searchByEmployerOnly(request);
        }

        // No valid search criteria
        SearchResponse response = SearchResponse.noResults(request);
        response.addSearchTip("Please provide at least employee name or employer ID");
        return response;
    }

    // ==================== SEARCH IMPLEMENTATIONS ====================

    /**
     * Search by National ID + Employer (highest precision)
     */
    private SearchResponse searchByNationalIdAndEmployer(SearchRequest request) {
        logger.debug("Executing national ID + employer search");

        List<EmploymentRecordDto> allRecords = getAllEmploymentRecords();

        List<SearchResult> results = allRecords.stream()
                .filter(record -> matchesNationalId(record, request.getNationalId()))
                .filter(record -> matchesEmployer(record, request.getEmployerId()))
                .map(record -> SearchResult.fromEmploymentRecord(record, "exact", 1.0))
                .collect(Collectors.toList());

        return buildSearchResponse(results, request, "Try checking National ID format or employer ID");
    }

    /**
     * Search by composite key (Name + Employer + Employment dates)
     */
    private SearchResponse searchByCompositeKey(SearchRequest request) {
        logger.debug("Executing composite key search");

        List<EmploymentRecordDto> allRecords = getAllEmploymentRecords();

        List<SearchResult> results = allRecords.stream()
                .filter(record -> matchesName(record, request.getEmployeeName(), "exact"))
                .filter(record -> matchesEmployer(record, request.getEmployerId()))
                .filter(record -> matchesEmploymentDates(record, request.getEmploymentStartDate(), request.getEmploymentEndDate()))
                .map(record -> SearchResult.fromEmploymentRecord(record, "exact", 1.0))
                .collect(Collectors.toList());

        return buildSearchResponse(results, request, "Try relaxing date criteria or check spelling");
    }

    /**
     * Search by Name + Employer (common case for ex-employee verification)
     */
    private SearchResponse searchByNameAndEmployer(SearchRequest request) {
        logger.debug("Executing name + employer search");

        List<EmploymentRecordDto> allRecords = getAllEmploymentRecords();
        String searchType = request.getSearchType() != null ? request.getSearchType() : "partial";

        List<SearchResult> exactMatches = new ArrayList<>();
        List<SearchResult> partialMatches = new ArrayList<>();
        List<SearchResult> fuzzyMatches = new ArrayList<>();

        // Categorize matches by precision
        for (EmploymentRecordDto record : allRecords) {
            if (!matchesEmployer(record, request.getEmployerId())) {
                continue;
            }

            if (matchesName(record, request.getEmployeeName(), "exact")) {
                exactMatches.add(SearchResult.fromEmploymentRecord(record, "exact", 1.0));
            } else if (matchesName(record, request.getEmployeeName(), "partial")) {
                partialMatches.add(SearchResult.fromEmploymentRecord(record, "partial", 0.8));
            } else if (request.getIncludeSimilar() && matchesName(record, request.getEmployeeName(), "fuzzy")) {
                double similarity = calculateNameSimilarity(
                        request.getEmployeeName().toLowerCase(),
                        record.getEmployeeName().getFullName().toLowerCase()
                );
                fuzzyMatches.add(SearchResult.fromEmploymentRecord(record, "fuzzy", similarity));
            }
        }

        // Select best match category based on search type
        List<SearchResult> finalResults = selectBestMatches(exactMatches, partialMatches, fuzzyMatches, searchType);

        // Apply additional filters and limit results
        finalResults = applyAdditionalFilters(finalResults, request);
        finalResults = limitResults(finalResults, request.getMaxResults());

        return buildSearchResponse(finalResults, request, "Try using partial matching or check name spelling");
    }

    /**
     * Search by name only (broadest search)
     */
    private SearchResponse searchByNameOnly(SearchRequest request) {
        logger.debug("Executing name-only search");

        List<EmploymentRecordDto> allRecords = getAllEmploymentRecords();

        List<SearchResult> results = allRecords.stream()
                .filter(record -> matchesName(record, request.getEmployeeName(), request.getSearchType()))
                .map(record -> {
                    String matchType = getNameMatchType(record, request.getEmployeeName());
                    double score = calculateMatchScore(record, request.getEmployeeName());
                    return SearchResult.fromEmploymentRecord(record, matchType, score);
                })
                .sorted((r1, r2) -> Double.compare(r2.getMatchScore(), r1.getMatchScore()))
                .limit(request.getMaxResults())
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            return SearchResponse.noResults(request);
        } else {
            SearchResponse response = SearchResponse.multipleResults(results, request);
            response.addSearchTip("Consider adding employer filter for more precise results");
            return response;
        }
    }

    /**
     * Search by employer only
     */
    private SearchResponse searchByEmployerOnly(SearchRequest request) {
        logger.debug("Executing employer-only search");

        List<EmploymentRecordDto> allRecords = getAllEmploymentRecords();

        List<SearchResult> results = allRecords.stream()
                .filter(record -> matchesEmployer(record, request.getEmployerId()))
                .map(record -> SearchResult.fromEmploymentRecord(record, "exact", 1.0))
                .limit(request.getMaxResults())
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            return SearchResponse.noResults(request);
        } else {
            SearchResponse response = SearchResponse.multipleResults(results, request);
            response.addSearchTip("All employees for this employer - add name filter to narrow results");
            return response;
        }
    }

    // ==================== DATA RETRIEVAL ====================

    /**
     * Get all employment records from blockchain
     */
    private List<EmploymentRecordDto> getAllEmploymentRecords() {
        List<EmploymentRecordDto> records = new ArrayList<>();

        try {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            // Scan current year records
            records.addAll(getRecordsForYear(currentYear));

            // Also check previous year records
            records.addAll(getRecordsForYear(currentYear - 1));

        } catch (Exception e) {
            logger.error("Error retrieving employment records for search", e);
        }

        logger.debug("Retrieved {} total employment records", records.size());
        return records;
    }

    /**
     * Get records for a specific year
     */
    private List<EmploymentRecordDto> getRecordsForYear(int year) {
        List<EmploymentRecordDto> records = new ArrayList<>();
        int totalRecords = getTotalRecordsCount(year);

        logger.debug("Scanning {} records for year {}", totalRecords, year);

        for (int counter = 1; counter <= totalRecords; counter++) {
            String employeeId = String.format("EMP-%d-%06d", year, counter);

            try {
                String result = fabricGatewayService.evaluateTransaction("getRecord", employeeId);
                EmploymentRecordDto record = objectMapper.readValue(result, EmploymentRecordDto.class);
                records.add(record);
            } catch (Exception e) {
                // Record not found - continue to next
                logger.trace("Record {} not found, continuing", employeeId);
            }
        }

        return records;
    }

    /**
     * Get total records count from system counter
     */
    private int getTotalRecordsCount(int year) {
        try {
            String counterResult = fabricGatewayService.evaluateTransaction("getEmployeeCounter", String.valueOf(year));

            if (counterResult != null && !counterResult.trim().isEmpty()) {
                JsonNode counterJson = objectMapper.readTree(counterResult);
                return counterJson.get("current_counter").asInt();
            }
        } catch (Exception e) {
            logger.warn("Could not get system counter for year {}: {}", year, e.getMessage());
        }

        return 0;
    }

    // ==================== MATCHING LOGIC ====================

    private boolean matchesNationalId(NameInfoDto record, String nationalId) {
        if (nationalId == null || record.getNationalId() == null) {
            return false;
        }
        return record.getNationalId().equalsIgnoreCase(nationalId);
    }

    private boolean matchesEmployer(EmploymentRecordDto record, String employerId) {
        if (employerId == null || record.getEmployerId() == null) {
            return false;
        }
        return record.getEmployerId().equalsIgnoreCase(employerId);
    }

    private boolean matchesName(EmploymentRecordDto record, String searchName, String matchType) {
        if (searchName == null || record.getEmployeeName() == null || record.getEmployeeName().getFullName() == null) {
            return false;
        }

        String recordName = record.getEmployeeName().getFullName().toLowerCase().trim();
        String queryName = searchName.toLowerCase().trim();

        switch (matchType) {
            case "exact":
                return recordName.equals(queryName);
            case "partial":
                return recordName.contains(queryName) || queryName.contains(recordName);
            case "fuzzy":
                return calculateNameSimilarity(recordName, queryName) >= FUZZY_MATCH_THRESHOLD;
            default:
                return recordName.contains(queryName);
        }
    }

    private boolean matchesEmploymentDates(EmploymentRecordDto record, String startDate, String endDate) {
        if (record.getTenure() == null) {
            return false;
        }

        // Simple date matching - in production you'd use proper date parsing
        if (startDate != null && record.getTenure().getStartDate() != null) {
            String datePrefix = startDate.substring(0, Math.min(7, startDate.length()));
            if (!record.getTenure().getStartDate().contains(datePrefix)) {
                return false;
            }
        }

        if (endDate != null && record.getTenure().getEndDate() != null) {
            String datePrefix = endDate.substring(0, Math.min(7, endDate.length()));
            if (!record.getTenure().getEndDate().contains(datePrefix)) {
                return false;
            }
        }

        return true;
    }

    // ==================== FILTERING AND PROCESSING ====================

    private List<SearchResult> selectBestMatches(List<SearchResult> exactMatches,
                                                 List<SearchResult> partialMatches,
                                                 List<SearchResult> fuzzyMatches,
                                                 String searchType) {
        List<SearchResult> finalResults = new ArrayList<>();

        if (!exactMatches.isEmpty()) {
            finalResults.addAll(exactMatches);
        } else if (!partialMatches.isEmpty() && !searchType.equals("exact")) {
            finalResults.addAll(partialMatches);
        } else if (!fuzzyMatches.isEmpty() && searchType.equals("fuzzy")) {
            finalResults.addAll(fuzzyMatches);
        }

        return finalResults;
    }

    private List<SearchResult> applyAdditionalFilters(List<SearchResult> results, SearchRequest request) {
        return results.stream()
                .filter(result -> matchesJobTitle(result, request.getJobTitle()))
                .filter(result -> matchesDepartment(result, request.getDepartment()))
                .filter(result -> matchesDateRange(result, request.getDateRangeStart(), request.getDateRangeEnd()))
                .collect(Collectors.toList());
    }

    private List<SearchResult> limitResults(List<SearchResult> results, int maxResults) {
        if (results.size() > maxResults) {
            return results.subList(0, maxResults);
        }
        return results;
    }

    private boolean matchesJobTitle(SearchResult result, String jobTitle) {
        if (jobTitle == null) return true;
        return result.getJobTitle() != null &&
                result.getJobTitle().toLowerCase().contains(jobTitle.toLowerCase());
    }

    private boolean matchesDepartment(SearchResult result, String department) {
        if (department == null) return true;
        return result.getDepartment() != null &&
                result.getDepartment().toLowerCase().contains(department.toLowerCase());
    }

    private boolean matchesDateRange(SearchResult result, String startRange, String endRange) {
        // Simple date range checking - implement proper date logic as needed
        return true;
    }

    // ==================== UTILITY METHODS ====================

    private SearchResponse buildSearchResponse(List<SearchResult> results, SearchRequest request, String noResultsTip) {
        if (results.isEmpty()) {
            SearchResponse response = SearchResponse.noResults(request);
            response.addSearchTip(noResultsTip);
            return response;
        } else if (results.size() == 1) {
            return SearchResponse.singleResult(results.get(0), request);
        } else {
            return SearchResponse.multipleResults(results, request);
        }
    }

    private String getNameMatchType(EmploymentRecordDto record, String searchName) {
        if (matchesName(record, searchName, "exact")) return "exact";
        if (matchesName(record, searchName, "partial")) return "partial";
        return "fuzzy";
    }

    private double calculateMatchScore(EmploymentRecordDto record, String searchName) {
        if (record.getEmployeeName() == null || record.getEmployeeName().getFullName() == null) {
            return 0.0;
        }

        return calculateNameSimilarity(
                record.getEmployeeName().getFullName().toLowerCase(),
                searchName.toLowerCase()
        );
    }

    private double calculateNameSimilarity(String name1, String name2) {
        if (name1 == null || name2 == null) return 0.0;
        if (name1.equals(name2)) return 1.0;

        int maxLength = Math.max(name1.length(), name2.length());
        if (maxLength == 0) return 1.0;

        int distance = levenshteinDistance(name1, name2);
        return 1.0 - (double) distance / maxLength;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}