package com.xxxzz.gmall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.xxxzz.gmall.manage.mapper")
@EnableTransactionManagement
@ComponentScan("com.xxxzz.gmall")
public class GmallManageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManageServiceApplication.class, args);
	}

}
