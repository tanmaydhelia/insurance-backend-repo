package com.insurance_billing.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance_billing.model.Payment;
import com.insurance_billing.service.BillingService;

@RestController
@RequestMapping("/billing")
public class BillingController {
	@Autowired
    private BillingService billingService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
        try {
            Double amount = Double.parseDouble(data.get("amount").toString());
            Integer userId = Integer.parseInt(data.get("userId").toString());
            Integer policyId = data.containsKey("policyId") ? Integer.parseInt(data.get("policyId").toString()) : null;

            Payment payment = billingService.createOrder(amount, userId, policyId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating order: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) {
        boolean isValid = billingService.verifyPayment(
                data.get("razorpay_order_id"),
                data.get("razorpay_payment_id"),
                data.get("razorpay_signature")
        );

        if (isValid) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Payment verified"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("status", "failed", "message", "Invalid signature"));
        }
    }
}
