package com.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.foxnic.springboot.starter.FoxnicApplication;

@SpringBootApplication
public class SBootApp {
	public static void main(String[] args) {
		FoxnicApplication.run(SBootApp.class, args);
	}
}
