package com.fairoz.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Embeddable
public class PaymentInstrument {
    
    @NotBlank(message = "Payment instrument type is required")
    private String type;
    
    @JsonProperty("card_number")
    private String cardNumber;
    
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/\\d{2}$", message = "Expiry must be in MM/YY format")
    private String expiry;
    
    private String cvv;
    
    @JsonProperty("holder_name")
    private String holderName;
    
    public PaymentInstrument() {}
    
    public PaymentInstrument(String type, String cardNumber, String expiry, String cvv, String holderName) {
        this.type = type;
        this.cardNumber = cardNumber;
        this.expiry = expiry;
        this.cvv = cvv;
        this.holderName = holderName;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getExpiry() {
        return expiry;
    }
    
    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
    
    public String getCvv() {
        return cvv;
    }
    
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    
    public String getHolderName() {
        return holderName;
    }
    
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
}