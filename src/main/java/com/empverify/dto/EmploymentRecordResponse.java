package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmploymentRecordResponse {
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("verification_timestamp")
    private String verificationTimestamp;

    @JsonProperty("validation_warnings")
    private List<String> validationWarnings;

    @JsonProperty("access_level")
    private String accessLevel;

    // Constructors
    public EmploymentRecordResponse() {}

    // Getters and Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getVerificationTimestamp() { return verificationTimestamp; }
    public void setVerificationTimestamp(String verificationTimestamp) { this.verificationTimestamp = verificationTimestamp; }
    public List<String> getValidationWarnings() { return validationWarnings; }
    public void setValidationWarnings(List<String> validationWarnings) { this.validationWarnings = validationWarnings; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}