package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentDto {
    @JsonProperty("s3_bucket")
    private String s3Bucket;

    @JsonProperty("s3_key")
    private String s3Key;

    @JsonProperty("s3_url")
    private String s3Url;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("access_level")
    private String accessLevel;

    @JsonProperty("upload_timestamp")
    private String uploadTimestamp;

    @JsonProperty("verification_timestamp")
    private String verificationTimestamp;

    @JsonProperty("file_hash")
    private String fileHash;

    @JsonProperty("file_size_bytes")
    private Long fileSizeBytes;

    @JsonProperty("expiry_date")
    private String expiryDate;

    @JsonProperty("review_period")
    private String reviewPeriod;

    @JsonProperty("related_incident_id")
    private String relatedIncidentId;

    @JsonProperty("reviewer")
    private NameInfoDto reviewer;

    @JsonProperty("issued_by")
    private NameInfoDto issuedBy;

    @JsonProperty("interviewer")
    private NameInfoDto interviewer;

    @JsonProperty("authorizing_officer")
    private NameInfoDto authorizingOfficer;

    // Constructors
    public DocumentDto() {}

    // Getters and Setters (shortened for brevity, but all properties should have them)
    public String getS3Bucket() { return s3Bucket; }
    public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public String getS3Url() { return s3Url; }
    public void setS3Url(String s3Url) { this.s3Url = s3Url; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
    public String getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(String uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }
    public String getVerificationTimestamp() { return verificationTimestamp; }
    public void setVerificationTimestamp(String verificationTimestamp) { this.verificationTimestamp = verificationTimestamp; }
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getReviewPeriod() { return reviewPeriod; }
    public void setReviewPeriod(String reviewPeriod) { this.reviewPeriod = reviewPeriod; }
    public String getRelatedIncidentId() { return relatedIncidentId; }
    public void setRelatedIncidentId(String relatedIncidentId) { this.relatedIncidentId = relatedIncidentId; }
    public NameInfoDto getReviewer() { return reviewer; }
    public void setReviewer(NameInfoDto reviewer) { this.reviewer = reviewer; }
    public NameInfoDto getIssuedBy() { return issuedBy; }
    public void setIssuedBy(NameInfoDto issuedBy) { this.issuedBy = issuedBy; }
    public NameInfoDto getInterviewer() { return interviewer; }
    public void setInterviewer(NameInfoDto interviewer) { this.interviewer = interviewer; }
    public NameInfoDto getAuthorizingOfficer() { return authorizingOfficer; }
    public void setAuthorizingOfficer(NameInfoDto authorizingOfficer) { this.authorizingOfficer = authorizingOfficer; }
}
