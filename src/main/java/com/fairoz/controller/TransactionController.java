package com.fairoz.controller;

import com.fairoz.dto.CallbackRequest;
import com.fairoz.dto.InitiateTransactionRequest;
import com.fairoz.dto.InitiateTransactionResponse;
import com.fairoz.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping("/initiate")
    public ResponseEntity<InitiateTransactionResponse> initiateTransaction(
            @Valid @RequestBody InitiateTransactionRequest request) {
        
        logger.info("Received transaction initiation request for order: {}", request.getOrderId());
        
        try {
            InitiateTransactionResponse response = transactionService.initiateTransaction(request);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for transaction initiation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (RuntimeException e) {
            logger.error("Error initiating transaction for order: {}", request.getOrderId(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            
        } catch (Exception e) {
            logger.error("Unexpected error initiating transaction for order: {}", request.getOrderId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/callback")
    public ResponseEntity<String> processCallback(@Valid @RequestBody CallbackRequest callbackRequest) {
        
        logger.info("Received callback for order: {} with status: {}", 
                   callbackRequest.getOrderId(), callbackRequest.getStatus());
        
        try {
            transactionService.processCallback(callbackRequest);
            return ResponseEntity.ok("Callback processed successfully");
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid callback request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
            
        } catch (Exception e) {
            logger.error("Error processing callback for order: {}", callbackRequest.getOrderId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing callback");
        }
    }
}