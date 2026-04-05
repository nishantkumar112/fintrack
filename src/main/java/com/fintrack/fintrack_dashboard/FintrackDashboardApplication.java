package com.fintrack.fintrack_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class FintrackDashboardApplication {

	public static void main(String[] args) {
        System.out.println("New Passwords is : "+ new BCryptPasswordEncoder().encode("password123"));

		SpringApplication.run(FintrackDashboardApplication.class, args);
	}

}
