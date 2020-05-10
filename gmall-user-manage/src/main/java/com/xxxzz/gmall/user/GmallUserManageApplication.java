package com.xxxzz.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.xxxzz.gmall.user.mapper")
@ComponentScan(basePackages = "com.xxxzz.gmall")
public class GmallUserManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUserManageApplication.class, args);
	}

}
