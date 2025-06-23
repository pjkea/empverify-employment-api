package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResult {

    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employer_id")
    private String employerId;

    @JsonProperty("employer_name")
    private String employerName;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("employment_start_date")
    private String employmentStartDate;

    @JsonProperty("employment_end_date")
    private String employmentEndDate;

    @JsonProperty("department")
    private String department;

    @JsonProperty("match_score")
    private Double matchScore; // Relevance score for ranking

    @JsonProperty("match_type")
    private String matchType; // exact, partial, fuzzy

    @JsonProperty("disambiguation_info")
    private DisambiguationInfo disambiguationInfo;

    @JsonProperty("national_id_partial")
    private String nationalIdPartial; // Last 4 digits for privacy

    @JsonProperty("eligible_for_rehire")
    private Boolean eligibleForRehire;

    // Constructors
    public SearchResult() {}

    public SearchResult(String employeeId, String employeeName, String employerId, String employerName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employerId = employerId;
        this.employerName = employerName;
    }

    // Static factory method for creating from EmploymentRecordDto
    public static SearchResult fromEmploymentRecord(EmploymentRecordDto record, String matchType, Double matchScore) {
        SearchResult result = new SearchResult();
        result.setEmployeeId(record.getEmployeeId());
        result.setEmployeeName(record.getEmployeeName() != null ? record.getEmployeeName().getFullName() : null);
        result.setEmployerId(record.getEmployerId());
        result.setEmployerName(record.getEmployerName());
        result.setJobTitle(record.getJobTitle());
        result.setEligibleForRehire(record.getEligibleForRehire());
        result.setMatchType(matchType);
        result.setMatchScore(matchScore);

        // Extract employment dates
        if (record.getTenure() != null) {
            result.setEmploymentStartDate(record.getTenure().getStartDate());
            result.setEmploymentEndDate(record.getTenure().getEndDate());
        }

        // Extract department
        if (record.getMetadata() != null) {
            result.setDepartment(record.getMetadata().getDepartment());
        }

        // Create disambiguation info
        DisambiguationInfo disambigInfo = new DisambiguationInfo();
        disambigInfo.setEmploymentPeriod(formatEmploymentPeriod(result.getEmploymentStartDate(), result.getEmploymentEndDate()));
        disambigInfo.setJobTitle(result.getJobTitle());
        disambigInfo.setDepartment(result.getDepartment());
        result.setDisambiguationInfo(disambigInfo);

        return result;
    }

    public static SearchResult fromEmploymentRecordWithNationalId(EmploymentRecordDto record, String matchType, Double matchScore) {
        // Create base SearchResult using existing method
        SearchResult result = fromEmploymentRecord(record, matchType, matchScore);

        // Add partial national ID for privacy (accessing through correct path)
        if (record.getEmployeeName() != null &&
                record.getEmployeeName().getNationalId() != null &&
                !record.getEmployeeName().getNationalId().trim().isEmpty()) {

            String nationalId = record.getEmployeeName().getNationalId().trim();

            if (nationalId.length() >= 4) {
                result.setNationalIdPartial("****" + nationalId.substring(nationalId.length() - 4));
            } else {
                result.setNationalIdPartial("****");
            }
        }

        return result;
    }


    // Helper method to format employment period
    private static String formatEmploymentPeriod(String startDate, String endDate) {
        if (startDate == null && endDate == null) {
            return "Unknown period";
        }

        String start = startDate != null ? startDate : "Unknown";
        String end = endDate != null ? endDate : "Present";

        return start + " to " + end;
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmploymentStartDate() {
        return employmentStartDate;
    }

    public void setEmploymentStartDate(String employmentStartDate) {
        this.employmentStartDate = employmentStartDate;
    }

    public String getEmploymentEndDate() {
        return employmentEndDate;
    }

    public void setEmploymentEndDate(String employmentEndDate) {
        this.employmentEndDate = employmentEndDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public DisambiguationInfo getDisambiguationInfo() {
        return disambiguationInfo;
    }

    public void setDisambiguationInfo(DisambiguationInfo disambiguationInfo) {
        this.disambiguationInfo = disambiguationInfo;
    }

    public String getNationalIdPartial() {
        return nationalIdPartial;
    }

    public void setNationalIdPartial(String nationalIdPartial) {
        this.nationalIdPartial = nationalIdPartial;
    }

    public Boolean getEligibleForRehire() {
        return eligibleForRehire;
    }

    public void setEligibleForRehire(Boolean eligibleForRehire) {
        this.eligibleForRehire = eligibleForRehire;
    }
}