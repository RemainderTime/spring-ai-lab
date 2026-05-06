package com.xf.rag.config;

import com.xf.aiagentchat.memory.RedisChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * AiConfig
 *
 * @author 海言
 * @date 2026/3/24
 * @time 16:39
 * @Description 配置Memory记忆模块
 */
@Configuration
public class AiConfig {

    /**
     * 创建一个基于Redis分布式的 ChatMemory 实例
     *
     * @param messages
     * @return
     */
    @Bean
    public ChatMemory chatMemory(StringRedisTemplate messages) {
        return new RedisChatMemory(messages, 50, 7);
    }

}
