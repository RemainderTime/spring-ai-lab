package com.xf.aiagentchat.config;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xf.aiagentchat.memory.RedisChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

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
     * 创建一个基于JVM内存的 ChatMemory 实例
     *
     * @return
     */
//    @Bean
//    public ChatMemory chatMemory() {
//        // 1. 创建底层的内存存储仓库 (相当于数据库)
//        InMemoryChatMemoryRepository repository = new InMemoryChatMemoryRepository();
//
//        // 2. 用 MessageWindowChatMemory 包装它，并设置全局最大消息保留条数
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(repository)
//                .maxMessages(20) // 保留最近的 20 条对话
//                .build();
//    }


    /**
     *  创建一个基于Redis分布式的 ChatMemory 实例
     * @param messages
     * @return
     */
    @Bean
    public ChatMemory chatMemory(RedisTemplate<String, List<Message>> messages) {
        return new RedisChatMemory(messages, 50, 7);
    }

    @Bean
    public RedisTemplate<String, List<Message>> aiMessageRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper springBootObjectMapper) { // 👇 核心：让 Spring 把调教好的满血版 ObjectMapper 注入进来！

        RedisTemplate<String, List<Message>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. Key 依然用 String
        template.setKeySerializer(new StringRedisSerializer());

        // 2. 【防污染机制】深度克隆一个 ObjectMapper。
        // 因为我们要修改它，如果不 copy，会把整个微服务其他接口的 JSON 格式全搞乱！
        ObjectMapper aiMapper = springBootObjectMapper.copy();

        // 3. 开启多态类型记录（告诉 Jackson 在 JSON 里写入 @class 字段，标明是 UserMessage 还是 AssistantMessage）
        aiMapper.activateDefaultTyping(
                aiMapper.getPolymorphicTypeValidator()
        );

        // 4. 将满血版 Mapper 交给 Redis 序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(aiMapper));

        template.afterPropertiesSet();
        return template;
    }
}
