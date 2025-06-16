package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class NoticePeriodDto {
    @JsonProperty("value")
    private String value;

    @JsonProperty("unit")
    private UnitDto unit;

    @JsonProperty("amount")
    private Integer amount;

    // Constructors
    public NoticePeriodDto() {}

    // Getters and Setters
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public UnitDto getUnit() { return unit; }
    public void setUnit(UnitDto unit) { this.unit = unit; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}