package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentCollectionDto {
    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("access_level")
    private String accessLevel;

    @JsonProperty("documents")
    private DocumentsDto documents;

    // Constructors
    public DocumentCollectionDto() {}

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
    public DocumentsDto getDocuments() { return documents; }
    public void setDocuments(DocumentsDto documents) { this.documents = documents; }
}