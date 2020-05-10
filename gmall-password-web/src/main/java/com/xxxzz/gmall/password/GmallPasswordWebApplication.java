package com.xxxzz.gmall.password;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.xxxzz.gmall")
public class GmallPasswordWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallPasswordWebApplication.class, args);
	}

}
