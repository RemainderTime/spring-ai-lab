package com.xf.rag;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {DashScopeChatAutoConfiguration.class})
public class AiAgentRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentRagApplication.class, args);
    }

}
