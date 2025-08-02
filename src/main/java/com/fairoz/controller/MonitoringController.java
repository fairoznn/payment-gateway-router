package com.fairoz.controller;

import com.fairoz.service.GatewayHealthService;
import com.fairoz.service.GatewayRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {
    
    private final GatewayHealthService gatewayHealthService;
    private final GatewayRoutingService gatewayRoutingService;
    
    @Autowired
    public MonitoringController(GatewayHealthService gatewayHealthService, 
                               GatewayRoutingService gatewayRoutingService) {
        this.gatewayHealthService = gatewayHealthService;
        this.gatewayRoutingService = gatewayRoutingService;
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getGatewayHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        List<String> healthyGateways = gatewayRoutingService.getHealthyGateways();
        Map<String, Double> successRates = gatewayHealthService.getGatewaySuccessRates();
        
        healthStatus.put("healthy_gateways", healthyGateways);
        healthStatus.put("success_rates", successRates);
        healthStatus.put("total_healthy_gateways", healthyGateways.size());
        
        return ResponseEntity.ok(healthStatus);
    }
}