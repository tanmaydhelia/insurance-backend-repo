package com.insurance_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class InsuranceNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsuranceNotificationServiceApplication.class, args);
	}

}
