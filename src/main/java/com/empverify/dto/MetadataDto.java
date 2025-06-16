package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class MetadataDto {
    @JsonProperty("department")
    private String department;

    @JsonProperty("immediate_supervisor")
    private NameInfoDto immediateSupervisor;

    @JsonProperty("employment_type")
    private EmploymentTypeDto employmentType;

    @JsonProperty("location")
    private String location;

    @JsonProperty("skills_verified")
    private List<String> skillsVerified;

    @JsonProperty("achievements")
    private List<String> achievements;

    @JsonProperty("disciplinary_records")
    private List<String> disciplinaryRecords;

    @JsonProperty("additional_fields")
    private AdditionalFieldsDto additionalFields;

    // Constructors
    public MetadataDto() {}

    // Getters and Setters
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public NameInfoDto getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(NameInfoDto immediateSupervisor) { this.immediateSupervisor = immediateSupervisor; }
    public EmploymentTypeDto getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentTypeDto employmentType) { this.employmentType = employmentType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public List<String> getSkillsVerified() { return skillsVerified; }
    public void setSkillsVerified(List<String> skillsVerified) { this.skillsVerified = skillsVerified; }
    public List<String> getAchievements() { return achievements; }
    public void setAchievements(List<String> achievements) { this.achievements = achievements; }
    public List<String> getDisciplinaryRecords() { return disciplinaryRecords; }
    public void setDisciplinaryRecords(List<String> disciplinaryRecords) { this.disciplinaryRecords = disciplinaryRecords; }
    public AdditionalFieldsDto getAdditionalFields() { return additionalFields; }
    public void setAdditionalFields(AdditionalFieldsDto additionalFields) { this.additionalFields = additionalFields; }
}