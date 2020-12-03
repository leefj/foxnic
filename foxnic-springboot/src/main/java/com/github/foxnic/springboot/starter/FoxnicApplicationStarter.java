package com.github.foxnic.springboot.starter;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.github.foxnic.springboot.Meta;

@SpringBootApplication
@ComponentScan(basePackages = {Meta.BASE_PACKAGE})
public class FoxnicApplicationStarter {

}
