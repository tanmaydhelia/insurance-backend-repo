// package com.insurance_billing.controller;

// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// import java.time.LocalDateTime;
// import java.util.HashMap;
// import java.util.Map;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.insurance_billing.model.Payment;
// import com.insurance_billing.service.BillingService;

// /**
//  * Controller tests for BillingController
//  * Target: 90%+ code coverage
//  */
// @WebMvcTest(BillingController.class)
// class BillingControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private BillingService billingService;

//     private Payment mockPayment;

//     @BeforeEach
//     void setUp() {
//         mockPayment = Payment.builder()
//                 .id(1)
//                 .razorpayOrderId("order_123456")
//                 .status("CREATED")
//                 .amount(500.0)
//                 .userId(10)
//                 .policyId(5)
//                 .createdAt(LocalDateTime.now())
//                 .build();
//     }

//     // ==================== CREATE ORDER TESTS ====================

//     @Test
//     void testCreateOrder_Success() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 500.0);
//         requestData.put("userId", 10);
//         requestData.put("policyId", 5);

//         when(billingService.createOrder(500.0, 10, 5)).thenReturn(mockPayment);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.razorpayOrderId").value("order_123456"))
//                 .andExpect(jsonPath("$.status").value("CREATED"))
//                 .andExpect(jsonPath("$.amount").value(500.0))
//                 .andExpect(jsonPath("$.userId").value(10))
//                 .andExpect(jsonPath("$.policyId").value(5));

//         verify(billingService, times(1)).createOrder(500.0, 10, 5);
//     }

//     @Test
//     void testCreateOrder_WithoutPolicyId() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 300.0);
//         requestData.put("userId", 15);
//         // No policyId

//         Payment paymentWithoutPolicy = Payment.builder()
//                 .id(2)
//                 .razorpayOrderId("order_789012")
//                 .status("CREATED")
//                 .amount(300.0)
//                 .userId(15)
//                 .policyId(null)
//                 .createdAt(LocalDateTime.now())
//                 .build();

//         when(billingService.createOrder(300.0, 15, null)).thenReturn(paymentWithoutPolicy);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.razorpayOrderId").value("order_789012"))
//                 .andExpect(jsonPath("$.userId").value(15))
//                 .andExpect(jsonPath("$.policyId").doesNotExist());

//         verify(billingService, times(1)).createOrder(300.0, 15, null);
//     }

//     @Test
//     void testCreateOrder_WithDecimalAmount() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 1250.75);
//         requestData.put("userId", 20);
//         requestData.put("policyId", 10);

//         Payment decimalPayment = Payment.builder()
//                 .id(3)
//                 .razorpayOrderId("order_decimal")
//                 .status("CREATED")
//                 .amount(1250.75)
//                 .userId(20)
//                 .policyId(10)
//                 .createdAt(LocalDateTime.now())
//                 .build();

//         when(billingService.createOrder(1250.75, 20, 10)).thenReturn(decimalPayment);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.amount").value(1250.75));

//         verify(billingService, times(1)).createOrder(1250.75, 20, 10);
//     }

//     @Test
//     void testCreateOrder_ServiceThrowsException() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 500.0);
//         requestData.put("userId", 10);
//         requestData.put("policyId", 5);

//         when(billingService.createOrder(anyDouble(), anyInt(), anyInt()))
//                 .thenThrow(new RuntimeException("Razorpay API error"));

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isInternalServerError())
//                 .andExpect(content().string(org.hamcrest.Matchers.containsString("Error creating order")))
//                 .andExpect(content().string(org.hamcrest.Matchers.containsString("Razorpay API error")));

//         verify(billingService, times(1)).createOrder(500.0, 10, 5);
//     }

//     @Test
//     void testCreateOrder_InvalidAmountFormat() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", "invalid");
//         requestData.put("userId", 10);
//         requestData.put("policyId", 5);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isInternalServerError())
//                 .andExpect(content().string(org.hamcrest.Matchers.containsString("Error creating order")));

//         verify(billingService, never()).createOrder(anyDouble(), anyInt(), anyInt());
//     }

//     @Test
//     void testCreateOrder_InvalidUserIdFormat() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 500.0);
//         requestData.put("userId", "not_a_number");
//         requestData.put("policyId", 5);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isInternalServerError())
//                 .andExpect(content().string(org.hamcrest.Matchers.containsString("Error creating order")));

//         verify(billingService, never()).createOrder(anyDouble(), anyInt(), anyInt());
//     }

//     @Test
//     void testCreateOrder_MissingAmount() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("userId", 10);
//         requestData.put("policyId", 5);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isInternalServerError());

//         verify(billingService, never()).createOrder(anyDouble(), anyInt(), anyInt());
//     }

//     @Test
//     void testCreateOrder_MissingUserId() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 500.0);
//         requestData.put("policyId", 5);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isInternalServerError());

//         verify(billingService, never()).createOrder(anyDouble(), anyInt(), anyInt());
//     }

//     @Test
//     void testCreateOrder_ZeroAmount() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 0.0);
//         requestData.put("userId", 10);
//         requestData.put("policyId", 5);

//         Payment zeroPayment = Payment.builder()
//                 .razorpayOrderId("order_zero")
//                 .status("CREATED")
//                 .amount(0.0)
//                 .userId(10)
//                 .policyId(5)
//                 .createdAt(LocalDateTime.now())
//                 .build();

//         when(billingService.createOrder(0.0, 10, 5)).thenReturn(zeroPayment);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.amount").value(0.0));

//         verify(billingService, times(1)).createOrder(0.0, 10, 5);
//     }

//     @Test
//     void testCreateOrder_LargeAmount() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 999999.99);
//         requestData.put("userId", 10);
//         requestData.put("policyId", 5);

//         Payment largePayment = Payment.builder()
//                 .razorpayOrderId("order_large")
//                 .status("CREATED")
//                 .amount(999999.99)
//                 .userId(10)
//                 .policyId(5)
//                 .createdAt(LocalDateTime.now())
//                 .build();

//         when(billingService.createOrder(999999.99, 10, 5)).thenReturn(largePayment);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.amount").value(999999.99));

//         verify(billingService, times(1)).createOrder(999999.99, 10, 5);
//     }

//     // ==================== VERIFY PAYMENT TESTS ====================

//     @Test
//     void testVerifyPayment_Success() throws Exception {
//         // Arrange
//         Map<String, String> verifyData = new HashMap<>();
//         verifyData.put("razorpay_order_id", "order_123456");
//         verifyData.put("razorpay_payment_id", "pay_789012");
//         verifyData.put("razorpay_signature", "valid_signature_hash");

//         when(billingService.verifyPayment("order_123456", "pay_789012", "valid_signature_hash"))
//                 .thenReturn(true);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("success"))
//                 .andExpect(jsonPath("$.message").value("Payment verified"));

//         verify(billingService, times(1))
//                 .verifyPayment("order_123456", "pay_789012", "valid_signature_hash");
//     }

//     @Test
//     void testVerifyPayment_InvalidSignature() throws Exception {
//         // Arrange
//         Map<String, String> verifyData = new HashMap<>();
//         verifyData.put("razorpay_order_id", "order_invalid");
//         verifyData.put("razorpay_payment_id", "pay_invalid");
//         verifyData.put("razorpay_signature", "invalid_signature");

//         when(billingService.verifyPayment("order_invalid", "pay_invalid", "invalid_signature"))
//                 .thenReturn(false);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.status").value("failed"))
//                 .andExpect(jsonPath("$.message").value("Invalid signature"));

//         verify(billingService, times(1))
//                 .verifyPayment("order_invalid", "pay_invalid", "invalid_signature");
//     }

//     @Test
//     void testVerifyPayment_MissingOrderId() throws Exception {
//         // Arrange
//         Map<String, String> verifyData = new HashMap<>();
//         verifyData.put("razorpay_payment_id", "pay_789012");
//         verifyData.put("razorpay_signature", "signature");

//         when(billingService.verifyPayment(null, "pay_789012", "signature"))
//                 .thenReturn(false);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.status").value("failed"));

//         verify(billingService, times(1)).verifyPayment(null, "pay_789012", "signature");
//     }

//     @Test
//     void testVerifyPayment_MissingPaymentId() throws Exception {
//         // Arrange
//         Map<String, String> verifyData = new HashMap<>();
//         verifyData.put("razorpay_order_id", "order_123456");
//         verifyData.put("razorpay_signature", "signature");

//         when(billingService.verifyPayment("order_123456", null, "signature"))
//                 .thenReturn(false);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isBadRequest());

//         verify(billingService, times(1)).verifyPayment("order_123456", null, "signature");
//     }

//     @Test
//     void testVerifyPayment_MissingSignature() throws Exception {
//         // Arrange
//         Map<String, String> verifyData = new HashMap<>();
//         verifyData.put("razorpay_order_id", "order_123456");
//         verifyData.put("razorpay_payment_id", "pay_789012");

//         when(billingService.verifyPayment("order_123456", "pay_789012", null))
//                 .thenReturn(false);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isBadRequest());

//         verify(billingService, times(1)).verifyPayment("order_123456", "pay_789012", null);
//     }

//     @Test
//     void testVerifyPayment_EmptyRequestBody() throws Exception {
//         // Arrange
//         Map<String, String> verifyData = new HashMap<>();

//         when(billingService.verifyPayment(null, null, null))
//                 .thenReturn(false);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isBadRequest());

//         verify(billingService, times(1)).verifyPayment(null, null, null);
//     }

//     @Test
//     void testVerifyPayment_ValidSignatureAfterMultipleAttempts() throws Exception {
//         // Arrange
//         Map<String, String> verifyData = new HashMap<>();
//         verifyData.put("razorpay_order_id", "order_retry");
//         verifyData.put("razorpay_payment_id", "pay_retry");
//         verifyData.put("razorpay_signature", "retry_signature");

//         when(billingService.verifyPayment("order_retry", "pay_retry", "retry_signature"))
//                 .thenReturn(true);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("success"));

//         verify(billingService, times(1))
//                 .verifyPayment("order_retry", "pay_retry", "retry_signature");
//     }

//     @Test
//     void testVerifyPayment_SpecialCharactersInSignature() throws Exception {
//         // Arrange
//         Map<String, String> verifyData = new HashMap<>();
//         verifyData.put("razorpay_order_id", "order_special");
//         verifyData.put("razorpay_payment_id", "pay_special");
//         verifyData.put("razorpay_signature", "abc!@#$%^&*()_+{}[]|:;<>?,./~`");

//         when(billingService.verifyPayment(anyString(), anyString(), anyString()))
//                 .thenReturn(true);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("success"));
//     }

//     @Test
//     void testVerifyPayment_LongSignatureString() throws Exception {
//         // Arrange
//         String longSignature = "a".repeat(500); // Very long signature
//         Map<String, String> verifyData = new HashMap<>();
//         verifyData.put("razorpay_order_id", "order_long");
//         verifyData.put("razorpay_payment_id", "pay_long");
//         verifyData.put("razorpay_signature", longSignature);

//         when(billingService.verifyPayment(anyString(), anyString(), anyString()))
//                 .thenReturn(true);

//         // Act & Assert
//         mockMvc.perform(post("/billing/verify")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(verifyData)))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     void testCreateOrder_NullPolicyIdInRequest() throws Exception {
//         // Arrange
//         Map<String, Object> requestData = new HashMap<>();
//         requestData.put("amount", 400.0);
//         requestData.put("userId", 12);
//         requestData.put("policyId", null);

//         Payment payment = Payment.builder()
//                 .razorpayOrderId("order_null_policy")
//                 .status("CREATED")
//                 .amount(400.0)
//                 .userId(12)
//                 .policyId(null)
//                 .createdAt(LocalDateTime.now())
//                 .build();

//         when(billingService.createOrder(400.0, 12, null)).thenReturn(payment);

//         // Act & Assert
//         mockMvc.perform(post("/billing/create-order")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(requestData)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.userId").value(12));

//         verify(billingService, times(1)).createOrder(400.0, 12, null);
//     }
// }
