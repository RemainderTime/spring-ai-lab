package com.xf.aiollamarag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AiOllamaRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiOllamaRagApplication.class, args);
    }

}
