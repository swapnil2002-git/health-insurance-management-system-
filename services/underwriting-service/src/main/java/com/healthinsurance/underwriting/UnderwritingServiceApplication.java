package com.healthinsurance.underwriting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class UnderwritingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnderwritingServiceApplication.class, args);
    }
}