package com.fairoz.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gateway_health_metrics")
public class GatewayHealthMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "gateway_name", nullable = false)
    private String gatewayName;
    
    @Column(name = "total_transactions", nullable = false)
    private Long totalTransactions = 0L;
    
    @Column(name = "successful_transactions", nullable = false)
    private Long successfulTransactions = 0L;
    
    @Column(name = "failed_transactions", nullable = false)
    private Long failedTransactions = 0L;
    
    @Column(name = "success_rate", nullable = false)
    private Double successRate = 100.0;
    
    @Column(name = "is_healthy", nullable = false)
    private Boolean isHealthy = true;
    
    @Column(name = "disabled_until")
    private LocalDateTime disabledUntil;
    
    @CreationTimestamp
    @Column(name = "window_start", nullable = false, updatable = false)
    private LocalDateTime windowStart;
    
    public GatewayHealthMetrics() {}
    
    public GatewayHealthMetrics(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    public void recordTransaction(boolean isSuccess) {
        this.totalTransactions++;
        if (isSuccess) {
            this.successfulTransactions++;
        } else {
            this.failedTransactions++;
        }
        this.successRate = totalTransactions > 0 ? 
            (successfulTransactions * 100.0) / totalTransactions : 100.0;
    }
    
    public void markAsUnhealthy(LocalDateTime disableUntil) {
        this.isHealthy = false;
        this.disabledUntil = disableUntil;
    }
    
    public void markAsHealthy() {
        this.isHealthy = true;
        this.disabledUntil = null;
    }
    
    public boolean isCurrentlyHealthy() {
        if (!isHealthy && disabledUntil != null) {
            return LocalDateTime.now().isAfter(disabledUntil);
        }
        return isHealthy;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    public Long getTotalTransactions() {
        return totalTransactions;
    }
    
    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
    
    public Long getSuccessfulTransactions() {
        return successfulTransactions;
    }
    
    public void setSuccessfulTransactions(Long successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }
    
    public Long getFailedTransactions() {
        return failedTransactions;
    }
    
    public void setFailedTransactions(Long failedTransactions) {
        this.failedTransactions = failedTransactions;
    }
    
    public Double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
    
    public Boolean getIsHealthy() {
        return isHealthy;
    }
    
    public void setIsHealthy(Boolean isHealthy) {
        this.isHealthy = isHealthy;
    }
    
    public LocalDateTime getDisabledUntil() {
        return disabledUntil;
    }
    
    public void setDisabledUntil(LocalDateTime disabledUntil) {
        this.disabledUntil = disabledUntil;
    }
    
    public LocalDateTime getWindowStart() {
        return windowStart;
    }
    
    public void setWindowStart(LocalDateTime windowStart) {
        this.windowStart = windowStart;
    }
}