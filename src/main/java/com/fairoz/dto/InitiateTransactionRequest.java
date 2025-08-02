package com.fairoz.dto;

import com.fairoz.model.PaymentInstrument;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class InitiateTransactionRequest {
    
    @JsonProperty("order_id")
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @Valid
    @JsonProperty("payment_instrument")
    @NotNull(message = "Payment instrument is required")
    private PaymentInstrument paymentInstrument;
    
    public InitiateTransactionRequest() {}
    
    public InitiateTransactionRequest(String orderId, BigDecimal amount, PaymentInstrument paymentInstrument) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentInstrument = paymentInstrument;
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
    
    public PaymentInstrument getPaymentInstrument() {
        return paymentInstrument;
    }
    
    public void setPaymentInstrument(PaymentInstrument paymentInstrument) {
        this.paymentInstrument = paymentInstrument;
    }
}