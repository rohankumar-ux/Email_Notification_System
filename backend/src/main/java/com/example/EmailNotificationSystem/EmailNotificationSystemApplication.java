package com.example.EmailNotificationSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EmailNotificationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailNotificationSystemApplication.class, args);
	}

}
