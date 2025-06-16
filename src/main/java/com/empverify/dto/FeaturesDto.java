package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeaturesDto {
    @JsonProperty("security")
    private List<String> security;

    @JsonProperty("validation")
    private List<String> validation;

    @JsonProperty("data_management")
    private List<String> dataManagement;

    // Constructors
    public FeaturesDto() {}

    // Getters and Setters
    public List<String> getSecurity() { return security; }
    public void setSecurity(List<String> security) { this.security = security; }
    public List<String> getValidation() { return validation; }
    public void setValidation(List<String> validation) { this.validation = validation; }
    public List<String> getDataManagement() { return dataManagement; }
    public void setDataManagement(List<String> dataManagement) { this.dataManagement = dataManagement; }
}