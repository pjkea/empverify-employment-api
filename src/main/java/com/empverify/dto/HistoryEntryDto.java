package com.empverify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryEntryDto {
    @JsonProperty("tx_id")
    private String txId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("is_delete")
    private Boolean isDelete;

    @JsonProperty("value")
    private Object value;

    // Constructors
    public HistoryEntryDto() {}

    // Getters and Setters
    public String getTxId() { return txId; }
    public void setTxId(String txId) { this.txId = txId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public Boolean getIsDelete() { return isDelete; }
    public void setIsDelete(Boolean isDelete) { this.isDelete = isDelete; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
}