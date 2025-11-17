package com.xf.aimodelschat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AiModelsChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiModelsChatApplication.class, args);
    }

}
