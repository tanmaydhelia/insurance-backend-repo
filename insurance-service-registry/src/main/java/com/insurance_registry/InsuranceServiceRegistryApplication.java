package com.insurance_registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class InsuranceServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsuranceServiceRegistryApplication.class, args);
	}

}
