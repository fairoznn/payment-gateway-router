package com.fairoz.config;

import com.fairoz.model.GatewayConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {
    
    private List<GatewayConfig> gateways;
    private HealthConfig health;
    
    public List<GatewayConfig> getGateways() {
        return gateways;
    }
    
    public void setGateways(List<GatewayConfig> gateways) {
        this.gateways = gateways;
    }
    
    public HealthConfig getHealth() {
        return health;
    }
    
    public void setHealth(HealthConfig health) {
        this.health = health;
    }
    
    public static class HealthConfig {
        private Double successRateThreshold;
        private Integer monitoringWindowMinutes;
        private Integer disableDurationMinutes;
        
        public Double getSuccessRateThreshold() {
            return successRateThreshold;
        }
        
        public void setSuccessRateThreshold(Double successRateThreshold) {
            this.successRateThreshold = successRateThreshold;
        }
        
        public Integer getMonitoringWindowMinutes() {
            return monitoringWindowMinutes;
        }
        
        public void setMonitoringWindowMinutes(Integer monitoringWindowMinutes) {
            this.monitoringWindowMinutes = monitoringWindowMinutes;
        }
        
        public Integer getDisableDurationMinutes() {
            return disableDurationMinutes;
        }
        
        public void setDisableDurationMinutes(Integer disableDurationMinutes) {
            this.disableDurationMinutes = disableDurationMinutes;
        }
    }
}