package com.ayoub.budgettracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BudgetTrackerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetTrackerApiApplication.class, args);
	}

}
