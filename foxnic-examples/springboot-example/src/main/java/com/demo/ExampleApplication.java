package com.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.foxnic.springboot.starter.FoxnicApplication;

@SpringBootApplication
public class ExampleApplication {
	public static void main(String[] args) {
		FoxnicApplication.run(ExampleApplication.class, args);
	}
}
