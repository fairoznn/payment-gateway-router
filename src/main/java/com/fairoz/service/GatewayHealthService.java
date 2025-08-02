package com.fairoz.service;

import com.fairoz.config.PaymentProperties;
import com.fairoz.model.GatewayHealthMetrics;
import com.fairoz.repository.GatewayHealthMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class GatewayHealthService {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayHealthService.class);
    
    private final GatewayHealthMetricsRepository healthMetricsRepository;
    private final PaymentProperties paymentProperties;
    
    @Autowired
    public GatewayHealthService(GatewayHealthMetricsRepository healthMetricsRepository, 
                               PaymentProperties paymentProperties) {
        this.healthMetricsRepository = healthMetricsRepository;
        this.paymentProperties = paymentProperties;
    }
    
    public void recordTransactionResult(String gatewayName, boolean isSuccess) {
        GatewayHealthMetrics currentMetrics = getCurrentOrCreateMetrics(gatewayName);
        currentMetrics.recordTransaction(isSuccess);
        
        evaluateGatewayHealth(currentMetrics);
        healthMetricsRepository.save(currentMetrics);
        
        logger.debug("Recorded transaction result for gateway: {} - Success: {} - Current success rate: {}%", 
                    gatewayName, isSuccess, currentMetrics.getSuccessRate());
    }
    
    public boolean isGatewayHealthy(String gatewayName) {
        GatewayHealthMetrics metrics = getCurrentOrCreateMetrics(gatewayName);
        boolean isHealthy = metrics.isCurrentlyHealthy();
        
        if (!isHealthy && metrics.getDisabledUntil() != null && 
            LocalDateTime.now().isAfter(metrics.getDisabledUntil())) {
            metrics.markAsHealthy();
            healthMetricsRepository.save(metrics);
            logger.info("Gateway {} is now healthy again after disabled period", gatewayName);
            return true;
        }
        
        return isHealthy;
    }
    
    public Map<String, Double> getGatewaySuccessRates() {
        LocalDateTime windowStart = LocalDateTime.now()
            .minusMinutes(paymentProperties.getHealth().getMonitoringWindowMinutes());
        
        return healthMetricsRepository.findAllByWindowStartAfter(windowStart)
            .stream()
            .collect(Collectors.groupingBy(
                GatewayHealthMetrics::getGatewayName,
                Collectors.averagingDouble(GatewayHealthMetrics::getSuccessRate)
            ));
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupOldMetrics() {
        LocalDateTime cutoff = LocalDateTime.now()
            .minusMinutes(paymentProperties.getHealth().getMonitoringWindowMinutes() * 2);
        
        List<GatewayHealthMetrics> oldMetrics = healthMetricsRepository.findAll()
            .stream()
            .filter(metrics -> metrics.getWindowStart().isBefore(cutoff))
            .collect(Collectors.toList());
        
        if (!oldMetrics.isEmpty()) {
            healthMetricsRepository.deleteAll(oldMetrics);
            logger.debug("Cleaned up {} old health metrics records", oldMetrics.size());
        }
    }
    
    private GatewayHealthMetrics getCurrentOrCreateMetrics(String gatewayName) {
        LocalDateTime windowStart = LocalDateTime.now()
            .minusMinutes(paymentProperties.getHealth().getMonitoringWindowMinutes());
        
        List<GatewayHealthMetrics> recentMetrics = healthMetricsRepository
            .findByGatewayNameAndWindowStartAfter(gatewayName, windowStart);
        
        if (recentMetrics.isEmpty()) {
            return new GatewayHealthMetrics(gatewayName);
        }
        
        return recentMetrics.get(0);
    }
    
    private void evaluateGatewayHealth(GatewayHealthMetrics metrics) {
        double threshold = paymentProperties.getHealth().getSuccessRateThreshold();
        
        if (metrics.getSuccessRate() < threshold && metrics.getTotalTransactions() >= 5) {
            LocalDateTime disableUntil = LocalDateTime.now()
                .plusMinutes(paymentProperties.getHealth().getDisableDurationMinutes());
            
            metrics.markAsUnhealthy(disableUntil);
            
            logger.warn("Gateway {} marked as unhealthy. Success rate: {}% (threshold: {}%). " +
                       "Disabled until: {}", 
                       metrics.getGatewayName(), metrics.getSuccessRate(), threshold, disableUntil);
        }
    }
}