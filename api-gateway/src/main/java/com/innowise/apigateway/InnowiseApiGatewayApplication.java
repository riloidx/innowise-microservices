package com.innowise.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InnowiseApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(InnowiseApiGatewayApplication.class, args);
    }

}
