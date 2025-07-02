package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmploymentRecordUpsertResponse {

    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("was_updated")
    private Boolean wasUpdated;

    @JsonProperty("operation")
    private String operation; // "created" or "updated"

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    // Constructors
    public EmploymentRecordUpsertResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public EmploymentRecordUpsertResponse(String employeeId, Boolean wasUpdated, String message) {
        this();
        this.employeeId = employeeId;
        this.wasUpdated = wasUpdated;
        this.operation = wasUpdated ? "updated" : "created";
        this.message = message;
    }

    // Static factory methods
    public static EmploymentRecordUpsertResponse created(String employeeId, String message) {
        return new EmploymentRecordUpsertResponse(employeeId, false, message);
    }

    public static EmploymentRecordUpsertResponse updated(String employeeId, String message) {
        return new EmploymentRecordUpsertResponse(employeeId, true, message);
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Boolean getWasUpdated() {
        return wasUpdated;
    }

    public void setWasUpdated(Boolean wasUpdated) {
        this.wasUpdated = wasUpdated;
        this.operation = wasUpdated ? "updated" : "created";
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}