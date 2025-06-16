package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class ReportingStructureDto {
    @JsonProperty("direct_reports")
    private List<PersonInfoDto> directReports;

    @JsonProperty("organizational_hierarchy")
    private List<HierarchyLevelDto> organizationalHierarchy;

    // Constructors
    public ReportingStructureDto() {}

    // Getters and Setters
    public List<PersonInfoDto> getDirectReports() { return directReports; }
    public void setDirectReports(List<PersonInfoDto> directReports) { this.directReports = directReports; }
    public List<HierarchyLevelDto> getOrganizationalHierarchy() { return organizationalHierarchy; }
    public void setOrganizationalHierarchy(List<HierarchyLevelDto> organizationalHierarchy) { this.organizationalHierarchy = organizationalHierarchy; }
}