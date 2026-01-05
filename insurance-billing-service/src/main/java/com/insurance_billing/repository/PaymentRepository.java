package com.insurance_billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance_billing.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer>{
	Payment findByRazorpayOrderId(String orderId);
}
