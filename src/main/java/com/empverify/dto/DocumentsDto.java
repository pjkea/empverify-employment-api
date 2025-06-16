package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class DocumentsDto {
    @JsonProperty("employment_contract")
    private DocumentDto employmentContract;

    @JsonProperty("resignation_letter")
    private DocumentDto resignationLetter;

    @JsonProperty("termination_letter")
    private DocumentDto terminationLetter;

    @JsonProperty("performance_reviews")
    private List<DocumentDto> performanceReviews;

    @JsonProperty("disciplinary_documents")
    private List<DocumentDto> disciplinaryDocuments;

    @JsonProperty("exit_interview")
    private DocumentDto exitInterview;

    @JsonProperty("id_verification")
    private DocumentDto idVerification;

    @JsonProperty("custom_documents")
    private List<DocumentDto> customDocuments;

    // Constructors
    public DocumentsDto() {}

    // Getters and Setters
    public DocumentDto getEmploymentContract() { return employmentContract; }
    public void setEmploymentContract(DocumentDto employmentContract) { this.employmentContract = employmentContract; }
    public DocumentDto getResignationLetter() { return resignationLetter; }
    public void setResignationLetter(DocumentDto resignationLetter) { this.resignationLetter = resignationLetter; }
    public DocumentDto getTerminationLetter() { return terminationLetter; }
    public void setTerminationLetter(DocumentDto terminationLetter) { this.terminationLetter = terminationLetter; }
    public List<DocumentDto> getPerformanceReviews() { return performanceReviews; }
    public void setPerformanceReviews(List<DocumentDto> performanceReviews) { this.performanceReviews = performanceReviews; }
    public List<DocumentDto> getDisciplinaryDocuments() { return disciplinaryDocuments; }
    public void setDisciplinaryDocuments(List<DocumentDto> disciplinaryDocuments) { this.disciplinaryDocuments = disciplinaryDocuments; }
    public DocumentDto getExitInterview() { return exitInterview; }
    public void setExitInterview(DocumentDto exitInterview) { this.exitInterview = exitInterview; }
    public DocumentDto getIdVerification() { return idVerification; }
    public void setIdVerification(DocumentDto idVerification) { this.idVerification = idVerification; }
    public List<DocumentDto> getCustomDocuments() { return customDocuments; }
    public void setCustomDocuments(List<DocumentDto> customDocuments) { this.customDocuments = customDocuments; }
}