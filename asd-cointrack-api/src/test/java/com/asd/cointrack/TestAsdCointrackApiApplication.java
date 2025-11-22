package com.asd.cointrack;

import org.springframework.boot.SpringApplication;

public class TestAsdCointrackApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AsdCointrackApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
