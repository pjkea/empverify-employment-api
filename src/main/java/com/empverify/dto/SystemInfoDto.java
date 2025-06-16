package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemInfoDto {
    @JsonProperty("message")
    private String message;

    @JsonProperty("features")
    private FeaturesDto features;

    @JsonProperty("collections")
    private Map<String, String> collections;

    @JsonProperty("access_levels")
    private Map<String, String> accessLevels;

    @JsonProperty("employee_id_format")
    private String employeeIdFormat;

    @JsonProperty("caller")
    private CallerInfoDto caller;

    // Constructors
    public SystemInfoDto() {}

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public FeaturesDto getFeatures() { return features; }
    public void setFeatures(FeaturesDto features) { this.features = features; }
    public Map<String, String> getCollections() { return collections; }
    public void setCollections(Map<String, String> collections) { this.collections = collections; }
    public Map<String, String> getAccessLevels() { return accessLevels; }
    public void setAccessLevels(Map<String, String> accessLevels) { this.accessLevels = accessLevels; }
    public String getEmployeeIdFormat() { return employeeIdFormat; }
    public void setEmployeeIdFormat(String employeeIdFormat) { this.employeeIdFormat = employeeIdFormat; }
    public CallerInfoDto getCaller() { return caller; }
    public void setCaller(CallerInfoDto caller) { this.caller = caller; }
}