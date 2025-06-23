package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {

    @JsonProperty("employee_name")
    @Size(min = 2, message = "Employee name must be at least 2 characters")
    private String employeeName;

    @JsonProperty("employer_id")
    private String employerId;

    @JsonProperty("employer_name")
    private String employerName;

    @JsonProperty("national_id")
    private String nationalId; // SSN/National ID for unique identification

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("employment_start_date")
    private String employmentStartDate; // Format: YYYY-MM-DD

    @JsonProperty("employment_end_date")
    private String employmentEndDate; // Format: YYYY-MM-DD

    @JsonProperty("date_range_start")
    private String dateRangeStart; // Search within employment period

    @JsonProperty("date_range_end")
    private String dateRangeEnd; // Search within employment period

    @JsonProperty("department")
    private String department;

    @JsonProperty("search_type")
    private String searchType = "partial"; // exact, partial, fuzzy

    @JsonProperty("max_results")
    private Integer maxResults = 10; // Limit search results

    @JsonProperty("include_similar")
    private Boolean includeSimilar = true; // Include fuzzy matches

    // Constructors
    public SearchRequest() {}

    // Static factory methods for common searches
    public static SearchRequest byNameAndEmployer(String employeeName, String employerId) {
        SearchRequest request = new SearchRequest();
        request.setEmployeeName(employeeName);
        request.setEmployerId(employerId);
        return request;
    }

    public static SearchRequest byNationalIdAndEmployer(String nationalId, String employerId) {
        SearchRequest request = new SearchRequest();
        request.setNationalId(nationalId);
        request.setEmployerId(employerId);
        request.setSearchType("exact");
        return request;
    }

    public static SearchRequest byCompositeKey(String employeeName, String employerId, String startDate, String endDate) {
        SearchRequest request = new SearchRequest();
        request.setEmployeeName(employeeName);
        request.setEmployerId(employerId);
        request.setEmploymentStartDate(startDate);
        request.setEmploymentEndDate(endDate);
        request.setSearchType("exact");
        return request;
    }

    // Getters and Setters
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

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
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

    public String getDateRangeStart() {
        return dateRangeStart;
    }

    public void setDateRangeStart(String dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }

    public String getDateRangeEnd() {
        return dateRangeEnd;
    }

    public void setDateRangeEnd(String dateRangeEnd) {
        this.dateRangeEnd = dateRangeEnd;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Boolean getIncludeSimilar() {
        return includeSimilar;
    }

    public void setIncludeSimilar(Boolean includeSimilar) {
        this.includeSimilar = includeSimilar;
    }
}