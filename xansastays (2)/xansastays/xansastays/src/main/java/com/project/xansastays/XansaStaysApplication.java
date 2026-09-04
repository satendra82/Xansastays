package com.project.xansastays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XansaStaysApplication {

	public static void main(String[] args) {
		SpringApplication.run(XansaStaysApplication.class, args);
	}

}
