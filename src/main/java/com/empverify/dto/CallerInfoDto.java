package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallerInfoDto {
    @JsonProperty("msp_id")
    private String mspId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("access_level")
    private String accessLevel;

    // Constructors
    public CallerInfoDto() {}

    // Getters and Setters
    public String getMspId() { return mspId; }
    public void setMspId(String mspId) { this.mspId = mspId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}