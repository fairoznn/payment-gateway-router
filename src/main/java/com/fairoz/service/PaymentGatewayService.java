package com.fairoz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentGatewayService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);
    private final Random random = new Random();
    
    public PaymentGatewayResponse processPayment(String gateway, String orderId, 
                                               double amount, Map<String, Object> paymentDetails) {
        
        logger.info("Processing payment through gateway: {} for order: {} amount: {}", 
                   gateway, orderId, amount);
        
        try {
            Thread.sleep(random.nextInt(100) + 50);
            
            String gatewayTransactionId = generateGatewayTransactionId(gateway);
            
            boolean isSuccess = simulateGatewayResponse(gateway);
            
            PaymentGatewayResponse response = new PaymentGatewayResponse(
                gatewayTransactionId, 
                isSuccess, 
                isSuccess ? null : "Simulated gateway failure"
            );
            
            logger.info("Gateway {} response for order {}: {} (txn_id: {})", 
                       gateway, orderId, isSuccess ? "SUCCESS" : "FAILURE", gatewayTransactionId);
            
            return response;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Payment processing interrupted for order: {}", orderId, e);
            return new PaymentGatewayResponse(null, false, "Processing interrupted");
        } catch (Exception e) {
            logger.error("Error processing payment through gateway: {} for order: {}", 
                        gateway, orderId, e);
            return new PaymentGatewayResponse(null, false, "Gateway processing error");
        }
    }
    
    private boolean simulateGatewayResponse(String gateway) {
        double successRate = switch (gateway.toLowerCase()) {
            case "razorpay" -> 0.95;
            case "payu" -> 0.90;
            case "cashfree" -> 0.92;
            default -> 0.85;
        };
        
        return random.nextDouble() < successRate;
    }
    
    private String generateGatewayTransactionId(String gateway) {
        return gateway.toUpperCase() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
    public static class PaymentGatewayResponse {
        private final String transactionId;
        private final boolean success;
        private final String errorMessage;
        
        public PaymentGatewayResponse(String transactionId, boolean success, String errorMessage) {
            this.transactionId = transactionId;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}