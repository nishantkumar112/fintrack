package com.fintrack.fintrack_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class FintrackDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(FintrackDashboardApplication.class, args);
	}

}
