package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class HierarchyLevelDto extends PersonInfoDto {
    @JsonProperty("level")
    private Integer level;

    // Constructors
    public HierarchyLevelDto() {}

    // Getters and Setters
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}