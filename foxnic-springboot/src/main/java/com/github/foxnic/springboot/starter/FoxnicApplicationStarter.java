package com.github.foxnic.springboot.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.github.foxnic.springboot.Meta;

@SpringBootApplication
@ComponentScan(basePackages = {Meta.BASE_PACKAGE})
public class FoxnicApplicationStarter {

	public static ConfigurableApplicationContext run(Class<?> bootType, String... args) {
		ConfigurableApplicationContext context=new SpringApplication(new Class[] {FoxnicApplicationStarter.class,bootType}).run(args);
		return context;
    }
	
}
