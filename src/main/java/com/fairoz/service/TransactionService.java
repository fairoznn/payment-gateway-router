package com.fairoz.service;

import com.fairoz.dto.CallbackRequest;
import com.fairoz.dto.InitiateTransactionRequest;
import com.fairoz.dto.InitiateTransactionResponse;
import com.fairoz.model.Transaction;
import com.fairoz.model.TransactionStatus;
import com.fairoz.repository.TransactionRepository;
import com.fairoz.service.PaymentGatewayService.PaymentGatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final GatewayRoutingService gatewayRoutingService;
    private final PaymentGatewayService paymentGatewayService;
    private final GatewayHealthService gatewayHealthService;
    
    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                             GatewayRoutingService gatewayRoutingService,
                             PaymentGatewayService paymentGatewayService,
                             GatewayHealthService gatewayHealthService) {
        this.transactionRepository = transactionRepository;
        this.gatewayRoutingService = gatewayRoutingService;
        this.paymentGatewayService = paymentGatewayService;
        this.gatewayHealthService = gatewayHealthService;
    }
    
    public InitiateTransactionResponse initiateTransaction(InitiateTransactionRequest request) {
        if (transactionRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalArgumentException("Transaction with order ID " + request.getOrderId() + " already exists");
        }
        
        String selectedGateway = gatewayRoutingService.selectGateway();
        
        Transaction transaction = new Transaction(
            request.getOrderId(), 
            request.getAmount(), 
            request.getPaymentInstrument()
        );
        transaction.setSelectedGateway(selectedGateway);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        logger.info("Transaction initiated: {} with gateway: {} for amount: {}", 
                   request.getOrderId(), selectedGateway, request.getAmount());
        
        try {
            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("type", request.getPaymentInstrument().getType());
            paymentDetails.put("cardNumber", request.getPaymentInstrument().getCardNumber());
            
            PaymentGatewayResponse gatewayResponse = paymentGatewayService.processPayment(
                selectedGateway, 
                request.getOrderId(), 
                request.getAmount().doubleValue(), 
                paymentDetails
            );
            
            if (gatewayResponse.getTransactionId() != null) {
                savedTransaction.setGatewayTransactionId(gatewayResponse.getTransactionId());
                transactionRepository.save(savedTransaction);
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment for transaction: {}", request.getOrderId(), e);
        }
        
        return new InitiateTransactionResponse(
            savedTransaction.getId(),
            savedTransaction.getOrderId(),
            savedTransaction.getAmount(),
            savedTransaction.getSelectedGateway(),
            savedTransaction.getStatus(),
            savedTransaction.getGatewayTransactionId(),
            savedTransaction.getCreatedAt()
        );
    }
    
    public void processCallback(CallbackRequest callbackRequest) {
        Transaction transaction = transactionRepository.findByOrderId(callbackRequest.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found for order ID: " + callbackRequest.getOrderId()));
        
        boolean isSuccess = "success".equals(callbackRequest.getStatus());
        TransactionStatus newStatus = isSuccess ? TransactionStatus.SUCCESS : TransactionStatus.FAILURE;
        
        transaction.setStatus(newStatus);
        if (!isSuccess && callbackRequest.getReason() != null) {
            transaction.setFailureReason(callbackRequest.getReason());
        }
        
        transactionRepository.save(transaction);
        
        gatewayHealthService.recordTransactionResult(callbackRequest.getGateway(), isSuccess);
        
        logger.info("Transaction callback processed: {} - Status: {} - Gateway: {}", 
                   callbackRequest.getOrderId(), newStatus, callbackRequest.getGateway());
        
        if (!isSuccess) {
            logger.warn("Transaction failed: {} - Reason: {}", 
                       callbackRequest.getOrderId(), callbackRequest.getReason());
        }
    }
}