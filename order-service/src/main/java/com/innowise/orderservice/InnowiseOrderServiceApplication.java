package com.innowise.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InnowiseOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InnowiseOrderServiceApplication.class, args);
    }

}
