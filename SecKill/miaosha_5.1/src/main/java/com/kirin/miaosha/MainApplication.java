package com.kirin.miaosha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(MainApplication.class, args);
	}
	
	//Spring Boot打Jwar包：继承SpringBootServletInitializer，重写configure()方法
//	@Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//        return builder.sources(MainApplication.class);
//    }
}
