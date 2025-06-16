package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class AdditionalFieldsDto {
    @JsonProperty("work_schedule")
    private String workSchedule;

    @JsonProperty("termination_type")
    private String terminationType;

    @JsonProperty("notice_period")
    private NoticePeriodDto noticePeriod;

    @JsonProperty("reason_category")
    private String reasonCategory;

    @JsonProperty("reporting_structure")
    private ReportingStructureDto reportingStructure;

    // Constructors
    public AdditionalFieldsDto() {}

    // Getters and Setters
    public String getWorkSchedule() { return workSchedule; }
    public void setWorkSchedule(String workSchedule) { this.workSchedule = workSchedule; }
    public String getTerminationType() { return terminationType; }
    public void setTerminationType(String terminationType) { this.terminationType = terminationType; }
    public NoticePeriodDto getNoticePeriod() { return noticePeriod; }
    public void setNoticePeriod(NoticePeriodDto noticePeriod) { this.noticePeriod = noticePeriod; }
    public String getReasonCategory() { return reasonCategory; }
    public void setReasonCategory(String reasonCategory) { this.reasonCategory = reasonCategory; }
    public ReportingStructureDto getReportingStructure() { return reportingStructure; }
    public void setReportingStructure(ReportingStructureDto reportingStructure) { this.reportingStructure = reportingStructure; }
}