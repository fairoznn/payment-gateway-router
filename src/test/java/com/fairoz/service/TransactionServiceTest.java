package com.fairoz.service;

import com.fairoz.dto.CallbackRequest;
import com.fairoz.dto.InitiateTransactionRequest;
import com.fairoz.dto.InitiateTransactionResponse;
import com.fairoz.model.PaymentInstrument;
import com.fairoz.model.Transaction;
import com.fairoz.model.TransactionStatus;
import com.fairoz.repository.TransactionRepository;
import com.fairoz.service.PaymentGatewayService.PaymentGatewayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private GatewayRoutingService gatewayRoutingService;
    
    @Mock
    private PaymentGatewayService paymentGatewayService;
    
    @Mock
    private GatewayHealthService gatewayHealthService;
    
    private TransactionService transactionService;
    
    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
            transactionRepository, gatewayRoutingService, 
            paymentGatewayService, gatewayHealthService
        );
    }
    
    @Test
    void testInitiateTransaction_Success() {
        PaymentInstrument paymentInstrument = new PaymentInstrument("card", "****1234", "12/25", "123", "John Doe");
        InitiateTransactionRequest request = new InitiateTransactionRequest("ORD123", BigDecimal.valueOf(499.0), paymentInstrument);
        
        Transaction savedTransaction = new Transaction("ORD123", BigDecimal.valueOf(499.0), paymentInstrument);
        savedTransaction.setId(1L);
        savedTransaction.setSelectedGateway("razorpay");
        savedTransaction.setCreatedAt(LocalDateTime.now());
        
        when(transactionRepository.existsByOrderId("ORD123")).thenReturn(false);
        when(gatewayRoutingService.selectGateway()).thenReturn("razorpay");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(paymentGatewayService.processPayment(anyString(), anyString(), anyDouble(), any()))
            .thenReturn(new PaymentGatewayResponse("RAZORPAY_12345", true, null));
        
        InitiateTransactionResponse response = transactionService.initiateTransaction(request);
        
        assertNotNull(response);
        assertEquals("ORD123", response.getOrderId());
        assertEquals(BigDecimal.valueOf(499.0), response.getAmount());
        assertEquals("razorpay", response.getSelectedGateway());
        assertEquals(TransactionStatus.PENDING, response.getStatus());
        
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
    
    @Test
    void testInitiateTransaction_DuplicateOrderId_ShouldThrowException() {
        PaymentInstrument paymentInstrument = new PaymentInstrument("card", "****1234", "12/25", "123", "John Doe");
        InitiateTransactionRequest request = new InitiateTransactionRequest("ORD123", BigDecimal.valueOf(499.0), paymentInstrument);
        
        when(transactionRepository.existsByOrderId("ORD123")).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> transactionService.initiateTransaction(request));
        
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(gatewayRoutingService, never()).selectGateway();
    }
    
    @Test
    void testProcessCallback_Success() {
        CallbackRequest callbackRequest = new CallbackRequest("ORD123", "success", "razorpay", null);
        
        Transaction transaction = new Transaction("ORD123", BigDecimal.valueOf(499.0), new PaymentInstrument());
        transaction.setSelectedGateway("razorpay");
        transaction.setStatus(TransactionStatus.PENDING);
        
        when(transactionRepository.findByOrderId("ORD123")).thenReturn(Optional.of(transaction));
        
        transactionService.processCallback(callbackRequest);
        
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        verify(transactionRepository).save(transaction);
        verify(gatewayHealthService).recordTransactionResult("razorpay", true);
    }
    
    @Test
    void testProcessCallback_Failure() {
        CallbackRequest callbackRequest = new CallbackRequest("ORD123", "failure", "razorpay", "Insufficient funds");
        
        Transaction transaction = new Transaction("ORD123", BigDecimal.valueOf(499.0), new PaymentInstrument());
        transaction.setSelectedGateway("razorpay");
        transaction.setStatus(TransactionStatus.PENDING);
        
        when(transactionRepository.findByOrderId("ORD123")).thenReturn(Optional.of(transaction));
        
        transactionService.processCallback(callbackRequest);
        
        assertEquals(TransactionStatus.FAILURE, transaction.getStatus());
        assertEquals("Insufficient funds", transaction.getFailureReason());
        verify(transactionRepository).save(transaction);
        verify(gatewayHealthService).recordTransactionResult("razorpay", false);
    }
    
    @Test
    void testProcessCallback_TransactionNotFound_ShouldThrowException() {
        CallbackRequest callbackRequest = new CallbackRequest("ORD999", "success", "razorpay", null);
        
        when(transactionRepository.findByOrderId("ORD999")).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> transactionService.processCallback(callbackRequest));
        
        verify(gatewayHealthService, never()).recordTransactionResult(anyString(), anyBoolean());
    }
}