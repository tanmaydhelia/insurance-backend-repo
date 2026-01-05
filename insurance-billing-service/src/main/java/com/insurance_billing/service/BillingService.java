package com.insurance_billing.service;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.insurance_billing.model.Payment;
import com.insurance_billing.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class BillingService {
	@Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private PaymentRepository paymentRepository;

    private String keySecret = "ITP0f47naRIM5M2kMduo4fW3";

    public Payment createOrder(Double amount, Integer userId, Integer policyId) throws Exception {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Amount in Paise (500.00 -> 50000)
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        Payment payment = Payment.builder()
                .razorpayOrderId(razorpayOrder.get("id"))
                .status("CREATED")
                .amount(amount)
                .userId(userId)
                .policyId(policyId)
                .createdAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                Payment payment = paymentRepository.findByRazorpayOrderId(orderId);
                if (payment != null) {
                    payment.setStatus("PAID");
                    payment.setRazorpayPaymentId(paymentId);
                    paymentRepository.save(payment);
                }
            }
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}
