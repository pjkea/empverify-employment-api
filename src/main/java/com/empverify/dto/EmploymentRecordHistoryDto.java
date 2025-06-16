package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmploymentRecordHistoryDto {
    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("access_level")
    private String accessLevel;

    @JsonProperty("history")
    private List<HistoryEntryDto> history;

    // Constructors
    public EmploymentRecordHistoryDto() {}

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
    public List<HistoryEntryDto> getHistory() { return history; }
    public void setHistory(List<HistoryEntryDto> history) { this.history = history; }
}