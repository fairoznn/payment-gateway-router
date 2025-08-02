package com.fairoz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CallbackRequest {
    
    @NotBlank(message = "Order ID is required")
    @JsonProperty("order_id")
    private String orderId;
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(success|failure)$", message = "Status must be either 'success' or 'failure'")
    private String status;
    
    @NotBlank(message = "Gateway is required")
    private String gateway;
    
    private String reason;
    
    public CallbackRequest() {}
    
    public CallbackRequest(String orderId, String status, String gateway, String reason) {
        this.orderId = orderId;
        this.status = status;
        this.gateway = gateway;
        this.reason = reason;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getGateway() {
        return gateway;
    }
    
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}