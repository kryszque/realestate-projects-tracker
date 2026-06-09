package com.mcdevka.realestate_projects_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RealestateProjectsTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealestateProjectsTrackerApplication.class, args);
	}

}
