package com.sonu.candidatehub;

import org.springframework.boot.SpringApplication;

public class TestCandidateManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(CandidateManagementApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
