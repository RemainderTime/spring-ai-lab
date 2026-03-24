package com.xf.aiagentchat.config;


import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiConfig
 *
 * @author 海言
 * @date 2026/3/24
 * @time 16:39
 * @Description 配置基于内存的聊天记忆配置
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        // 1. 创建底层的内存存储仓库 (相当于数据库)
        InMemoryChatMemoryRepository repository = new InMemoryChatMemoryRepository();

        // 2. 用 MessageWindowChatMemory 包装它，并设置全局最大消息保留条数
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20) // 保留最近的 20 条对话
                .build();
    }
}
