package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisambiguationInfo {

    @JsonProperty("employment_period")
    private String employmentPeriod; // "2020-01-15 to 2022-12-30"

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("department")
    private String department;

    @JsonProperty("tenure_months")
    private Integer tenureMonths;

    @JsonProperty("departure_reason")
    private String departureReason;

    @JsonProperty("performance_rating")
    private Double performanceRating;

    @JsonProperty("location")
    private String location;

    @JsonProperty("supervisor_name")
    private String supervisorName;

    @JsonProperty("rehire_eligible")
    private Boolean rehireEligible;

    @JsonProperty("verification_status")
    private String verificationStatus; // verified, pending, unverified

    // Constructors
    public DisambiguationInfo() {}

    public DisambiguationInfo(String employmentPeriod, String jobTitle, String department) {
        this.employmentPeriod = employmentPeriod;
        this.jobTitle = jobTitle;
        this.department = department;
    }

    // Helper method to create comprehensive disambiguation info
    public static DisambiguationInfo fromEmploymentRecord(EmploymentRecordDto record) {
        DisambiguationInfo info = new DisambiguationInfo();

        // Employment period
        if (record.getTenure() != null) {
            String start = record.getTenure().getStartDate() != null ? record.getTenure().getStartDate() : "Unknown";
            String end = record.getTenure().getEndDate() != null ? record.getTenure().getEndDate() : "Present";
            info.setEmploymentPeriod(start + " to " + end);
            info.setTenureMonths(record.getTenure().getDurationMonths());
        }

        // Basic info
        info.setJobTitle(record.getJobTitle());
        info.setPerformanceRating(record.getPerformanceRating());
        info.setRehireEligible(record.getEligibleForRehire());

        // Departure reason
        if (record.getDepartureReason() != null) {
            info.setDepartureReason(record.getDepartureReason().getValue());
        }

        // Metadata info
        if (record.getMetadata() != null) {
            info.setDepartment(record.getMetadata().getDepartment());
            info.setLocation(record.getMetadata().getLocation());

            // Supervisor info
            if (record.getMetadata().getImmediateSupervisor() != null) {
                info.setSupervisorName(record.getMetadata().getImmediateSupervisor().getFullName());
            }
        }

        // Verification status
        info.setVerificationStatus(record.getVerificationTimestamp() != null ? "verified" : "unverified");

        return info;
    }

    // Helper method to create summary for multiple matches
    public String createSummary() {
        StringBuilder summary = new StringBuilder();

        if (jobTitle != null) {
            summary.append(jobTitle);
        }

        if (department != null) {
            if (!summary.isEmpty()) summary.append(" in ");
            summary.append(department);
        }

        if (employmentPeriod != null) {
            if (!summary.isEmpty()) summary.append(" (");
            summary.append(employmentPeriod);
            if (!summary.isEmpty()) summary.append(")");
        }

        return summary.toString();
    }

    // Getters and Setters
    public String getEmploymentPeriod() {
        return employmentPeriod;
    }

    public void setEmploymentPeriod(String employmentPeriod) {
        this.employmentPeriod = employmentPeriod;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public String getDepartureReason() {
        return departureReason;
    }

    public void setDepartureReason(String departureReason) {
        this.departureReason = departureReason;
    }

    public Double getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(Double performanceRating) {
        this.performanceRating = performanceRating;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public Boolean getRehireEligible() {
        return rehireEligible;
    }

    public void setRehireEligible(Boolean rehireEligible) {
        this.rehireEligible = rehireEligible;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}