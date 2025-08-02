package com.fairoz.service;

import com.fairoz.config.PaymentProperties;
import com.fairoz.model.GatewayConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayRoutingServiceTest {
    
    @Mock
    private PaymentProperties paymentProperties;
    
    @Mock
    private GatewayHealthService gatewayHealthService;
    
    private GatewayRoutingService gatewayRoutingService;
    
    @BeforeEach
    void setUp() {
        gatewayRoutingService = new GatewayRoutingService(paymentProperties, gatewayHealthService);
    }
    
    @Test
    void testSelectGateway_WithHealthyGateways_ShouldReturnGateway() {
        List<GatewayConfig> gateways = Arrays.asList(
            new GatewayConfig("razorpay", 40, true),
            new GatewayConfig("payu", 35, true),
            new GatewayConfig("cashfree", 25, true)
        );
        
        when(paymentProperties.getGateways()).thenReturn(gateways);
        when(gatewayHealthService.isGatewayHealthy("razorpay")).thenReturn(true);
        when(gatewayHealthService.isGatewayHealthy("payu")).thenReturn(true);
        when(gatewayHealthService.isGatewayHealthy("cashfree")).thenReturn(true);
        
        String selectedGateway = gatewayRoutingService.selectGateway();
        
        assertNotNull(selectedGateway);
        assertTrue(Arrays.asList("razorpay", "payu", "cashfree").contains(selectedGateway));
    }
    
    @Test
    void testSelectGateway_WithNoHealthyGateways_ShouldThrowException() {
        List<GatewayConfig> gateways = Arrays.asList(
            new GatewayConfig("razorpay", 40, true),
            new GatewayConfig("payu", 35, true)
        );
        
        when(paymentProperties.getGateways()).thenReturn(gateways);
        when(gatewayHealthService.isGatewayHealthy("razorpay")).thenReturn(false);
        when(gatewayHealthService.isGatewayHealthy("payu")).thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> gatewayRoutingService.selectGateway());
    }
    
    @Test
    void testSelectGateway_WithDisabledGateways_ShouldExcludeDisabled() {
        List<GatewayConfig> gateways = Arrays.asList(
            new GatewayConfig("razorpay", 40, false),
            new GatewayConfig("payu", 35, true),
            new GatewayConfig("cashfree", 25, true)
        );
        
        when(paymentProperties.getGateways()).thenReturn(gateways);
        when(gatewayHealthService.isGatewayHealthy("payu")).thenReturn(true);
        when(gatewayHealthService.isGatewayHealthy("cashfree")).thenReturn(true);
        
        String selectedGateway = gatewayRoutingService.selectGateway();
        
        assertNotNull(selectedGateway);
        assertTrue(Arrays.asList("payu", "cashfree").contains(selectedGateway));
        assertNotEquals("razorpay", selectedGateway);
    }
    
    @Test
    void testGetHealthyGateways_ShouldReturnOnlyHealthyEnabled() {
        List<GatewayConfig> gateways = Arrays.asList(
            new GatewayConfig("razorpay", 40, true),
            new GatewayConfig("payu", 35, false),
            new GatewayConfig("cashfree", 25, true)
        );
        
        when(paymentProperties.getGateways()).thenReturn(gateways);
        when(gatewayHealthService.isGatewayHealthy("razorpay")).thenReturn(true);
        when(gatewayHealthService.isGatewayHealthy("cashfree")).thenReturn(false);
        
        List<String> healthyGateways = gatewayRoutingService.getHealthyGateways();
        
        assertEquals(1, healthyGateways.size());
        assertTrue(healthyGateways.contains("razorpay"));
    }
}