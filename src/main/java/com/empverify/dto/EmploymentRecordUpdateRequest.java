package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

// EmploymentRecordUpdateRequest
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmploymentRecordUpdateRequest {
    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("tenure")
    private TenureDto tenure;

    @JsonProperty("performance_rating")
    private Double performanceRating;

    @JsonProperty("departure_reason")
    private DepartureReasonDto departureReason;

    @JsonProperty("eligible_for_rehire")
    private Boolean eligibleForRehire;

    @JsonProperty("verifier_id")
    private String verifierId;

    @JsonProperty("verifier_name")
    private NameInfoDto verifierName;

    @JsonProperty("metadata")
    private MetadataDto metadata;

    // Constructors
    public EmploymentRecordUpdateRequest() {}

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public TenureDto getTenure() { return tenure; }
    public void setTenure(TenureDto tenure) { this.tenure = tenure; }
    public Double getPerformanceRating() { return performanceRating; }
    public void setPerformanceRating(Double performanceRating) { this.performanceRating = performanceRating; }
    public DepartureReasonDto getDepartureReason() { return departureReason; }
    public void setDepartureReason(DepartureReasonDto departureReason) { this.departureReason = departureReason; }
    public Boolean getEligibleForRehire() { return eligibleForRehire; }
    public void setEligibleForRehire(Boolean eligibleForRehire) { this.eligibleForRehire = eligibleForRehire; }
    public String getVerifierId() { return verifierId; }
    public void setVerifierId(String verifierId) { this.verifierId = verifierId; }
    public NameInfoDto getVerifierName() { return verifierName; }
    public void setVerifierName(NameInfoDto verifierName) { this.verifierName = verifierName; }
    public MetadataDto getMetadata() { return metadata; }
    public void setMetadata(MetadataDto metadata) { this.metadata = metadata; }
}