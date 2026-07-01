package com.offerflow.copilot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.offerflow.copilot.persistence.mapper")
public class OfferFlowCopilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfferFlowCopilotApplication.class, args);
    }
}
