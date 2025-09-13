package com.test.containers;

import org.springframework.boot.SpringApplication;

public class TestContainersApplication {

	public static void main(String[] args) {
		SpringApplication.from(ContainersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
