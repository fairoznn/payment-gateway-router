package com.fairoz.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fairoz.model.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InitiateTransactionResponse {
    
    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("order_id")
    private String orderId;

    private BigDecimal amount;

    @JsonProperty("selected_gateway")
    private String selectedGateway;
    private TransactionStatus status;

    @JsonProperty("gateway_transaction_id")
    private String gatewayTransactionId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    public InitiateTransactionResponse() {}
    
    public InitiateTransactionResponse(Long transactionId, String orderId, BigDecimal amount, 
                                     String selectedGateway, TransactionStatus status, 
                                     String gatewayTransactionId, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.amount = amount;
        this.selectedGateway = selectedGateway;
        this.status = status;
        this.gatewayTransactionId = gatewayTransactionId;
        this.createdAt = createdAt;
    }
    
    public Long getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getSelectedGateway() {
        return selectedGateway;
    }
    
    public void setSelectedGateway(String selectedGateway) {
        this.selectedGateway = selectedGateway;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
    
    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}