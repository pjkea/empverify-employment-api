package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmploymentRecordDto {

    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("employee_name")
    private NameInfoDto employeeName;

    @JsonProperty("employer_id")
    private String employerId;

    @JsonProperty("employer_name")
    private String employerName;

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

    @JsonProperty("verification_timestamp")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime verificationTimestamp;

    @JsonProperty("verifier_id")
    private String verifierId;

    @JsonProperty("verifier_name")
    private NameInfoDto verifierName;

    @JsonProperty("documents")
    private DocumentsDto documents;

    @JsonProperty("metadata")
    private MetadataDto metadata;

    // Default constructor
    public EmploymentRecordDto() {}

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

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

    public LocalDateTime getVerificationTimestamp() {
        return verificationTimestamp;
    }

    public void setVerificationTimestamp(LocalDateTime verificationTimestamp) {
        this.verificationTimestamp = verificationTimestamp;
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