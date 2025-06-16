package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmploymentRecordRequest {

    @JsonProperty("employee_name")
    @NotNull(message = "Employee name is required")
    @Valid
    private NameInfoDto employeeName;

    @JsonProperty("employer_id")
    @NotBlank(message = "Employer ID is required")
    private String employerId;

    @JsonProperty("employer_name")
    @NotBlank(message = "Employer name is required")
    private String employerName;

    @JsonProperty("job_title")
    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @JsonProperty("tenure")
    @Valid
    private TenureDto tenure;

    @JsonProperty("performance_rating")
    @DecimalMin(value = "0.0", message = "Performance rating must be between 0 and 10")
    @DecimalMax(value = "10.0", message = "Performance rating must be between 0 and 10")
    private Double performanceRating;

    @JsonProperty("departure_reason")
    @Valid
    private DepartureReasonDto departureReason;

    @JsonProperty("eligible_for_rehire")
    private Boolean eligibleForRehire;

    @JsonProperty("verifier_id")
    private String verifierId;

    @JsonProperty("verifier_name")
    @Valid
    private NameInfoDto verifierName;

    @JsonProperty("documents")
    @Valid
    private DocumentsDto documents;

    @JsonProperty("metadata")
    @Valid
    private MetadataDto metadata;

    // Default constructor
    public EmploymentRecordRequest() {}

    // Getters and Setters
    public NameInfoDto getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(NameInfoDto employeeName) {
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

    public TenureDto getTenure() {
        return tenure;
    }

    public void setTenure(TenureDto tenure) {
        this.tenure = tenure;
    }

    public Double getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(Double performanceRating) {
        this.performanceRating = performanceRating;
    }

    public DepartureReasonDto getDepartureReason() {
        return departureReason;
    }

    public void setDepartureReason(DepartureReasonDto departureReason) {
        this.departureReason = departureReason;
    }

    public Boolean getEligibleForRehire() {
        return eligibleForRehire;
    }

    public void setEligibleForRehire(Boolean eligibleForRehire) {
        this.eligibleForRehire = eligibleForRehire;
    }

    public String getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(String verifierId) {
        this.verifierId = verifierId;
    }

    public NameInfoDto getVerifierName() {
        return verifierName;
    }

    public void setVerifierName(NameInfoDto verifierName) {
        this.verifierName = verifierName;
    }

    public DocumentsDto getDocuments() {
        return documents;
    }

    public void setDocuments(DocumentsDto documents) {
        this.documents = documents;
    }

    public MetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataDto metadata) {
        this.metadata = metadata;
    }
}