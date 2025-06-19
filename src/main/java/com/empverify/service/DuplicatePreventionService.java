package com.empverify.service;

import com.empverify.dto.DuplicateCheckDto;
import com.empverify.dto.DuplicateCheckRequest;
import com.empverify.dto.EmploymentRecordDto;
import com.empverify.dto.NameInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicatePreventionService {

    private static final Logger logger = LoggerFactory.getLogger(DuplicatePreventionService.class);

    private final FabricGatewayService fabricGatewayService;
    private final ObjectMapper objectMapper;

    @Value("${empverify.duplicate-prevention.enabled:true}")
    private boolean duplicatePreventionEnabled;

    @Value("${empverify.duplicate-prevention.strict-mode:true}")
    private boolean strictMode;

    @Value("${empverify.duplicate-prevention.check-similar-names:true}")
    private boolean checkSimilarNames;

    @Autowired
    public DuplicatePreventionService(FabricGatewayService fabricGatewayService, ObjectMapper objectMapper) {
        this.fabricGatewayService = fabricGatewayService;
        this.objectMapper = objectMapper;
    }

    /**
     * Check for duplicate records before creating new employment record
     * OPTIMIZED: Uses system counter + name-first filtering
     */
    public DuplicateCheckDto checkForDuplicates(DuplicateCheckRequest request) {
        if (!duplicatePreventionEnabled) {
            logger.debug("Duplicate prevention is disabled, allowing record creation");
            return DuplicateCheckDto.noDuplicate();
        }

        logger.info("OPTIMIZED duplicate check: employee='{}', employer='{}', level='{}'",
                request.getEmployeeName().getFullName(), request.getEmployerId(), request.getCheckLevel());

        try {
            String targetEmployeeName = request.getEmployeeName().getFullName();
            String employerId = request.getEmployerId();

            // STEP 1: Filter by name first, then check employer (much more efficient)
            List<EmploymentRecordDto> matchingRecords = getAllRecordsForEmployer(employerId, targetEmployeeName);

            if (matchingRecords.isEmpty()) {
                logger.debug("No matching records found for employee '{}' at employer '{}'",
                        targetEmployeeName, employerId);
                return DuplicateCheckDto.noDuplicate();
            }

            // STEP 2: Check for exact matches in the filtered results
            List<String> exactMatches = findExactMatchesInResults(request, matchingRecords);
            if (!exactMatches.isEmpty()) {
                logger.warn("Exact duplicate found for employee '{}' at employer '{}': {}",
                        targetEmployeeName, employerId, exactMatches);
                return DuplicateCheckDto.exactMatch(exactMatches);
            }

            // STEP 3: Check for similar matches based on effective check level
            String effectiveCheckLevel = determineEffectiveCheckLevel(request.getCheckLevel());
            if (shouldCheckSimilarNames(effectiveCheckLevel)) {
                List<String> similarMatches = findSimilarMatchesInResults(request, matchingRecords);
                if (!similarMatches.isEmpty()) {
                    logger.info("Similar matches found for employee '{}' at employer '{}': {}",
                            targetEmployeeName, employerId, similarMatches);
                    return DuplicateCheckDto.similarMatch(similarMatches, "similar_name_match");
                }
            }

            logger.debug("No duplicates found for employee '{}' at employer '{}'",
                    targetEmployeeName, employerId);
            return DuplicateCheckDto.noDuplicate();

        } catch (Exception e) {
            logger.error("Error checking for duplicates", e);
            // In case of error, allow creation but log the issue
            return DuplicateCheckDto.noDuplicate();
        }
    }

    /**
     * Find exact matches in the pre-filtered results (much smaller list now)
     */
    private List<String> findExactMatchesInResults(DuplicateCheckRequest request, List<EmploymentRecordDto> filteredRecords) {
        String requestName = normalizeEmployeeName(request.getEmployeeName());
        String excludeId = request.getExcludeEmployeeId();

        return filteredRecords.stream()
                .filter(record -> !record.getEmployeeId().equals(excludeId)) // Exclude the specified ID
                .filter(record -> {
                    String existingName = normalizeEmployeeName(record.getEmployeeName());
                    return requestName.equals(existingName);
                })
                .map(EmploymentRecordDto::getEmployeeId)
                .collect(Collectors.toList());
    }

    /**
     * Find similar matches in the pre-filtered results (much smaller list now)
     */
    private List<String> findSimilarMatchesInResults(DuplicateCheckRequest request, List<EmploymentRecordDto> filteredRecords) {
        String requestName = normalizeEmployeeName(request.getEmployeeName());
        String excludeId = request.getExcludeEmployeeId();

        return filteredRecords.stream()
                .filter(record -> !record.getEmployeeId().equals(excludeId)) // Exclude the specified ID
                .filter(record -> {
                    String existingName = normalizeEmployeeName(record.getEmployeeName());
                    return calculateNameSimilarity(requestName, existingName) >= 0.8; // 80% similarity threshold
                })
                .map(EmploymentRecordDto::getEmployeeId)
                .collect(Collectors.toList());
    }

    /**
     * Determine the effective check level based on request and application config
     */
    private String determineEffectiveCheckLevel(String requestLevel) {
        if (requestLevel != null && !requestLevel.trim().isEmpty()) {
            return requestLevel.toLowerCase();
        }

        // Use application configuration as default
        if (strictMode) {
            return "strict";
        } else {
            return "moderate";
        }
    }

    /**
     * Determine if similar name checking should be performed based on check level
     */
    private boolean shouldCheckSimilarNames(String checkLevel) {
        return checkSimilarNames && ("strict".equals(checkLevel) || "moderate".equals(checkLevel));
    }

    /**
     * Get all employment records for a specific employer using optimized name-first filtering
     * Uses system counter to determine total records, then filters by name first
     */
    private List<EmploymentRecordDto> getAllRecordsForEmployer(String employerId, String targetEmployeeName) {
        List<EmploymentRecordDto> matchingRecords = new ArrayList<>();

        try {
            // Step 1: Get current counter to know total number of records
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int totalRecords = getTotalRecordsCount(currentYear);

            if (totalRecords == 0) {
                logger.debug("No existing records found (counter is 0)");
                return matchingRecords;
            }

            logger.debug("Scanning {} total records for name matches, then checking employer: {}",
                    totalRecords, employerId);

            // Step 2: Scan all records, filter by name FIRST (much faster)
            List<EmploymentRecordDto> nameMatches = findRecordsByName(targetEmployeeName, totalRecords, currentYear);

            // Step 3: From name matches, filter by employer (small subset now)
            for (EmploymentRecordDto record : nameMatches) {
                if (employerId.equals(record.getEmployerId())) {
                    matchingRecords.add(record);
                }
            }

        } catch (Exception e) {
            logger.error("Error retrieving records for employer: {}", employerId, e);
            // In case of error, return empty list to allow operation
        }

        logger.debug("Found {} matching records for employee '{}' at employer: {}",
                matchingRecords.size(), targetEmployeeName, employerId);
        return matchingRecords;
    }

    /**
     * Get total number of records from system counter
     */
    private int getTotalRecordsCount(int year) {
        try {
            String counterResult = fabricGatewayService.evaluateTransaction("getEmployeeCounter", String.valueOf(year));

            if (counterResult != null && !counterResult.trim().isEmpty()) {
                JsonNode counterJson = objectMapper.readTree(counterResult);
                int currentCounter = counterJson.get("current_counter").asInt();
                logger.debug("System counter shows {} total records for year {}", currentCounter, year);
                return currentCounter;
            }
        } catch (Exception e) {
            logger.warn("Could not get system counter, using fallback method: {}", e.getMessage());
        }

        // Fallback: Check if any records exist by trying first few IDs
        return estimateRecordCount(year);
    }

    /**
     * Fallback method to estimate record count if counter fails
     */
    private int estimateRecordCount(int year) {
        int lastFoundRecord = 0;

        // Binary search approach to find approximate count
        int low = 1, high = 1000;

        while (low <= high) {
            int mid = (low + high) / 2;
            String employeeId = String.format("EMP-%d-%06d", year, mid);

            try {
                fabricGatewayService.evaluateTransaction("getRecord", employeeId);
                lastFoundRecord = mid;
                low = mid + 1; // Record exists, try higher
            } catch (Exception e) {
                high = mid - 1; // Record doesn't exist, try lower
            }
        }

        logger.debug("Estimated record count: {}", lastFoundRecord);
        return lastFoundRecord;
    }

    /**
     * Find all records that match the target employee name (step 1 of filtering)
     */
    private List<EmploymentRecordDto> findRecordsByName(String targetEmployeeName, int totalRecords, int year) {
        List<EmploymentRecordDto> nameMatches = new ArrayList<>();
        String normalizedTargetName = targetEmployeeName.toLowerCase().trim();

        // Scan through all records, checking name first (fastest filter)
        for (int counter = 1; counter <= totalRecords; counter++) {
            String employeeId = String.format("EMP-%d-%06d", year, counter);

            try {
                String result = fabricGatewayService.evaluateTransaction("getRecord", employeeId);
                EmploymentRecordDto record = objectMapper.readValue(result, EmploymentRecordDto.class);

                // Quick name check FIRST (before any other processing)
                if (record.getEmployeeName() != null && record.getEmployeeName().getFullName() != null) {
                    String recordName = record.getEmployeeName().getFullName().toLowerCase().trim();

                    // Check for exact or similar name match
                    if (isNameMatch(normalizedTargetName, recordName)) {
                        nameMatches.add(record);
                        logger.debug("Name match found: '{}' (ID: {})", recordName, employeeId);
                    }
                }

            } catch (Exception e) {
                // Record not found or error - continue to next ID
                // This is expected for some employee IDs
            }
        }

        logger.debug("Found {} records with matching names for '{}'", nameMatches.size(), targetEmployeeName);
        return nameMatches;
    }

    /**
     * Check if two names match (exact or similar)
     */
    private boolean isNameMatch(String targetName, String recordName) {
        // Exact match
        if (targetName.equals(recordName)) {
            return true;
        }

        // Similar match (if enabled)
        if (checkSimilarNames) {
            double similarity = calculateNameSimilarity(targetName, recordName);
            return similarity >= 0.8; // 80% similarity threshold
        }

        return false;
    }

    /**
     * Find exact matches based on full name and employer
     */
    private List<String> findExactMatches(DuplicateCheckRequest request, List<EmploymentRecordDto> existingRecords) {
        // This method is now replaced by findExactMatchesInResults for better performance
        return findExactMatchesInResults(request, existingRecords);
    }

    /**
     * Find similar matches using fuzzy matching
     */
    private List<String> findSimilarMatches(DuplicateCheckRequest request, List<EmploymentRecordDto> existingRecords) {
        // This method is now replaced by findSimilarMatchesInResults for better performance
        return findSimilarMatchesInResults(request, existingRecords);
    }

    /**
     * Normalize employee name for comparison
     */
    private String normalizeEmployeeName(NameInfoDto nameInfo) {
        if (nameInfo == null || nameInfo.getFullName() == null) {
            return "";
        }

        return nameInfo.getFullName()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }

    /**
     * Calculate similarity between two names using Levenshtein distance
     */
    private double calculateNameSimilarity(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return 0.0;
        }

        if (name1.equals(name2)) {
            return 1.0;
        }

        int maxLength = Math.max(name1.length(), name2.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance(name1, name2);
        return 1.0 - (double) distance / maxLength;
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Check if duplicate prevention should block the operation
     */
    public boolean shouldBlockDuplicate(DuplicateCheckDto duplicateCheck) {
        if (!duplicatePreventionEnabled || duplicateCheck == null || !duplicateCheck.getIsDuplicate()) {
            return false;
        }

        // Always block exact matches regardless of mode
        if ("exact_name_employer_match".equals(duplicateCheck.getMatchCriteria())) {
            return true;
        }

        // For similar matches, check the confidence level and application settings
        if ("similar_name_match".equals(duplicateCheck.getMatchCriteria())) {
            // In strict mode, block similar matches
            // In moderate/loose mode, allow similar matches (with warnings)
            return strictMode && "high".equals(duplicateCheck.getConfidenceLevel());
        }

        return false;
    }

    /**
     * Check if duplicate prevention should block the operation based on check level
     */
    public boolean shouldBlockDuplicate(DuplicateCheckDto duplicateCheck, String checkLevel) {
        if (!duplicatePreventionEnabled || duplicateCheck == null || !duplicateCheck.getIsDuplicate()) {
            return false;
        }

        String effectiveLevel = determineEffectiveCheckLevel(checkLevel);

        // Always block exact matches regardless of level
        if ("exact_name_employer_match".equals(duplicateCheck.getMatchCriteria())) {
            return true;
        }

        // For similar matches, check based on level
        if ("similar_name_match".equals(duplicateCheck.getMatchCriteria())) {
            switch (effectiveLevel) {
                case "strict":
                    return true; // Block similar matches
                case "moderate":
                    return false; // Allow similar matches with warnings
                case "loose":
                    return false; // Allow all duplicates with warnings
                default:
                    return strictMode; // Fall back to application config
            }
        }

        return false;
    }

    /**
     * Get configuration info for duplicate prevention
     */
    public Map<String, Object> getConfigurationInfo() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", duplicatePreventionEnabled);
        config.put("strict_mode", strictMode);
        config.put("check_similar_names", checkSimilarNames);

        return config;
    }

    /**
     * Public method to check for duplicates with EmploymentRecordRequest data
     */
    public DuplicateCheckDto checkForDuplicates(NameInfoDto employeeName, String employerId) {
        DuplicateCheckRequest request = new DuplicateCheckRequest(employeeName, employerId);
        return checkForDuplicates(request);
    }

    /**
     * Public method to check for duplicates during updates (excludes current employee ID)
     */
    public DuplicateCheckDto checkForDuplicatesOnUpdate(NameInfoDto employeeName, String employerId, String currentEmployeeId) {
        DuplicateCheckRequest request = new DuplicateCheckRequest(employeeName, employerId);
        request.setExcludeEmployeeId(currentEmployeeId);
        return checkForDuplicates(request);
    }
}