package com.fairoz.controller;

import com.fairoz.PaymentGatewayRouterApplication;
import com.fairoz.dto.CallbackRequest;
import com.fairoz.dto.InitiateTransactionRequest;
import com.fairoz.model.PaymentInstrument;
import com.fairoz.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PaymentGatewayRouterApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        transactionRepository.deleteAll();
    }
    
    @Test
    void testInitiateTransaction_Success() throws Exception {
        PaymentInstrument paymentInstrument = new PaymentInstrument("card", "****1234", "12/25", "123", "John Doe");
        InitiateTransactionRequest request = new InitiateTransactionRequest("ORD123", BigDecimal.valueOf(499.0), paymentInstrument);
        
        mockMvc.perform(post("/transactions/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order_id").value("ORD123"))
                .andExpect(jsonPath("$.amount").value(499.0))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.selected_gateway").isNotEmpty());
    }
    
    @Test
    void testInitiateTransaction_ValidationError() throws Exception {
        PaymentInstrument paymentInstrument = new PaymentInstrument("", "", "", "", "");
        InitiateTransactionRequest request = new InitiateTransactionRequest("", BigDecimal.valueOf(-100), paymentInstrument);
        
        mockMvc.perform(post("/transactions/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    void testInitiateTransaction_DuplicateOrderId() throws Exception {
        PaymentInstrument paymentInstrument = new PaymentInstrument("card", "****1234", "12/25", "123", "John Doe");
        InitiateTransactionRequest request = new InitiateTransactionRequest("ORD123", BigDecimal.valueOf(499.0), paymentInstrument);
        
        mockMvc.perform(post("/transactions/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        mockMvc.perform(post("/transactions/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testProcessCallback_Success() throws Exception {
        PaymentInstrument paymentInstrument = new PaymentInstrument("card", "****1234", "12/25", "123", "John Doe");
        InitiateTransactionRequest initiateRequest = new InitiateTransactionRequest("ORD124", BigDecimal.valueOf(299.0), paymentInstrument);
        
        mockMvc.perform(post("/transactions/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initiateRequest)))
                .andExpect(status().isOk());
        
        CallbackRequest callbackRequest = new CallbackRequest("ORD124", "success", "razorpay", null);
        
        mockMvc.perform(post("/transactions/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Callback processed successfully"));
    }
    
    @Test
    void testProcessCallback_TransactionNotFound() throws Exception {
        CallbackRequest callbackRequest = new CallbackRequest("ORD999", "success", "razorpay", null);
        
        mockMvc.perform(post("/transactions/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request")));
    }
}