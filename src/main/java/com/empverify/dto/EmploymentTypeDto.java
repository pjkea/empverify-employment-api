package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class EmploymentTypeDto {
    @JsonProperty("value")
    private String value;

    @JsonProperty("allowed_options")
    private List<String> allowedOptions;

    // Constructors
    public EmploymentTypeDto() {}

    // Getters and Setters
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public List<String> getAllowedOptions() { return allowedOptions; }
    public void setAllowedOptions(List<String> allowedOptions) { this.allowedOptions = allowedOptions; }
}