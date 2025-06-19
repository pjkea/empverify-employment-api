package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DuplicateCheckDto {

    @JsonProperty("is_duplicate")
    private Boolean isDuplicate;

    @JsonProperty("message")
    private String message;

    @JsonProperty("existing_employee_ids")
    private List<String> existingEmployeeIds;

    @JsonProperty("match_criteria")
    private String matchCriteria;

    @JsonProperty("confidence_level")
    private String confidenceLevel;

    // Constructors
    public DuplicateCheckDto() {}

    public DuplicateCheckDto(Boolean isDuplicate, String message) {
        this.isDuplicate = isDuplicate;
        this.message = message;
    }

    public DuplicateCheckDto(Boolean isDuplicate, String message, List<String> existingEmployeeIds, String matchCriteria) {
        this.isDuplicate = isDuplicate;
        this.message = message;
        this.existingEmployeeIds = existingEmployeeIds;
        this.matchCriteria = matchCriteria;
    }

    // Static factory methods for common responses
    public static DuplicateCheckDto noDuplicate() {
        return new DuplicateCheckDto(false, "No duplicate records found");
    }

    public static DuplicateCheckDto exactMatch(List<String> existingIds) {
        DuplicateCheckDto dto = new DuplicateCheckDto(true, "Exact match found - employee already exists for this employer");
        dto.setExistingEmployeeIds(existingIds);
        dto.setMatchCriteria("exact_name_employer_match");
        dto.setConfidenceLevel("high");
        return dto;
    }

    public static DuplicateCheckDto similarMatch(List<String> existingIds, String criteria) {
        DuplicateCheckDto dto = new DuplicateCheckDto(true, "Similar record found - potential duplicate");
        dto.setExistingEmployeeIds(existingIds);
        dto.setMatchCriteria(criteria);
        dto.setConfidenceLevel("medium");
        return dto;
    }

    // Getters and Setters
    public Boolean getIsDuplicate() {
        return isDuplicate;
    }

    public void setIsDuplicate(Boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getExistingEmployeeIds() {
        return existingEmployeeIds;
    }

    public void setExistingEmployeeIds(List<String> existingEmployeeIds) {
        this.existingEmployeeIds = existingEmployeeIds;
    }

    public String getMatchCriteria() {
        return matchCriteria;
    }

    public void setMatchCriteria(String matchCriteria) {
        this.matchCriteria = matchCriteria;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
}