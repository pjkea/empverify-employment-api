package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeCounterDto {
    @JsonProperty("year")
    private Integer year;

    @JsonProperty("current_counter")
    private Integer currentCounter;

    @JsonProperty("next_employee_id")
    private String nextEmployeeId;

    // Constructors
    public EmployeeCounterDto() {}

    // Getters and Setters
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getCurrentCounter() { return currentCounter; }
    public void setCurrentCounter(Integer currentCounter) { this.currentCounter = currentCounter; }
    public String getNextEmployeeId() { return nextEmployeeId; }
    public void setNextEmployeeId(String nextEmployeeId) { this.nextEmployeeId = nextEmployeeId; }
}