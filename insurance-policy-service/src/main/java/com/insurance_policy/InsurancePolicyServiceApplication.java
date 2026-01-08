package com.insurance_policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InsurancePolicyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsurancePolicyServiceApplication.class, args);
	}

}
