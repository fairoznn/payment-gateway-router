package com.fairoz.service;

import com.fairoz.config.PaymentProperties;
import com.fairoz.model.GatewayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class GatewayRoutingService {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayRoutingService.class);
    
    private final PaymentProperties paymentProperties;
    private final GatewayHealthService gatewayHealthService;
    private final Random random;
    
    @Autowired
    public GatewayRoutingService(PaymentProperties paymentProperties, 
                                GatewayHealthService gatewayHealthService) {
        this.paymentProperties = paymentProperties;
        this.gatewayHealthService = gatewayHealthService;
        this.random = new Random();
    }
    
    public String selectGateway() {
        List<GatewayConfig> availableGateways = getAvailableGateways();
        
        if (availableGateways.isEmpty()) {
            logger.error("No healthy gateways available for routing");
            throw new RuntimeException("No healthy gateways available");
        }
        
        String selectedGateway = performWeightedSelection(availableGateways);
        
        logger.info("Selected gateway: {} from {} available gateways", 
                   selectedGateway, availableGateways.size());
        
        return selectedGateway;
    }
    
    public List<String> getHealthyGateways() {
        return getAvailableGateways()
            .stream()
            .map(GatewayConfig::getName)
            .collect(Collectors.toList());
    }
    
    private List<GatewayConfig> getAvailableGateways() {
        return paymentProperties.getGateways()
            .stream()
            .filter(gateway -> gateway.getEnabled())
            .filter(gateway -> gatewayHealthService.isGatewayHealthy(gateway.getName()))
            .collect(Collectors.toList());
    }
    
    private String performWeightedSelection(List<GatewayConfig> gateways) {
        int totalWeight = gateways.stream()
            .mapToInt(GatewayConfig::getWeight)
            .sum();
        
        if (totalWeight == 0) {
            return gateways.get(random.nextInt(gateways.size())).getName();
        }
        
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;
        
        for (GatewayConfig gateway : gateways) {
            currentWeight += gateway.getWeight();
            if (randomWeight < currentWeight) {
                logger.debug("Gateway selection: {} selected with weight {} (total weight: {}, random: {})", 
                           gateway.getName(), gateway.getWeight(), totalWeight, randomWeight);
                return gateway.getName();
            }
        }
        
        return gateways.get(gateways.size() - 1).getName();
    }
}