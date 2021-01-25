package com.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.foxnic.springboot.starter.FoxnicApplicationStarter;

@SpringBootApplication
public class SBootApp {
	public static void main(String[] args) {
		FoxnicApplicationStarter.run(SBootApp.class, args);
	}
}
