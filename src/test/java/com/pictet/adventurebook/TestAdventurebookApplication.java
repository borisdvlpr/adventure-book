package com.pictet.adventurebook;

import org.springframework.boot.SpringApplication;

public class TestAdventurebookApplication {

	public static void main(String[] args) {
		SpringApplication.from(AdventurebookApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
