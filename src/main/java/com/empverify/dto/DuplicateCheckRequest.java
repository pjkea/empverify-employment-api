package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DuplicateCheckRequest {

    @JsonProperty("employee_name")
    @NotNull(message = "Employee name is required for duplicate checking")
    private NameInfoDto employeeName;

    @JsonProperty("employer_id")
    @NotBlank(message = "Employer ID is required for duplicate checking")
    private String employerId;

    @JsonProperty("check_level")
    private String checkLevel = "moderate"; // strict, moderate, loose

    @JsonProperty("exclude_employee_id")
    private String excludeEmployeeId; // For updates - exclude this ID from duplicate check

    // Constructors
    public DuplicateCheckRequest() {}

    public DuplicateCheckRequest(NameInfoDto employeeName, String employerId) {
        this.employeeName = employeeName;
        this.employerId = employerId;
    }

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

    public String getCheckLevel() {
        return checkLevel;
    }

    public void setCheckLevel(String checkLevel) {
        this.checkLevel = checkLevel;
    }

    public String getExcludeEmployeeId() {
        return excludeEmployeeId;
    }

    public void setExcludeEmployeeId(String excludeEmployeeId) {
        this.excludeEmployeeId = excludeEmployeeId;
    }
}