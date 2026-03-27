package com.xf.aiagentchat.memory;


import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RedisChatMomery
 *
 * @author 海言
 * @date 2026/3/27
 * @time 15:47
 * @Description redis分布式记忆会话操作类
 */

public class RedisChatMemory implements ChatMemory {

    private static RedisTemplate<String, List<Message>> redisTemplate = new RedisTemplate<>();
    // memory 最大存储数
    private static int MAX_MEMORY_SIZE = 1000;
    // memory redis key 前缀
    private static String REDIS_KEY_PREFIX = "ai:agent:memory:userId:";
    // memory 过期时间
    private long expireDays;


    public RedisChatMemory(RedisTemplate<String, List<Message>> redisTemplate, int maxMessages, long expireDays) {
        RedisChatMemory.redisTemplate = redisTemplate;
        MAX_MEMORY_SIZE = maxMessages;
        this.expireDays = expireDays;
    }

    /**
     * 添加memory
     *
     * @param conversationId
     * @param messages
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> memory = this.get(conversationId);
        //追加memory
        memory.addAll(messages);
        // 如果超过最大存储数，则截取最新的 MAX_MEMORY_SIZE 条数据
        if (memory.size() > MAX_MEMORY_SIZE) {
            memory = memory.subList(memory.size() - MAX_MEMORY_SIZE, memory.size());
        }
        // 保存到 Redis 中
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + conversationId, memory, expireDays, TimeUnit.DAYS);
    }

    /**
     * 获取memory
     *
     * @param conversationId
     * @return
     */
    @Override
    public List<Message> get(String conversationId) {
        List<Message> messages = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + conversationId);
        if (messages == null) {
            return new ArrayList<>();
        }
        //按需返回最大存储数
        return messages.subList(Math.max(0, messages.size() - MAX_MEMORY_SIZE), messages.size());
    }

    /**
     * 清空memory
     *
     * @param conversationId
     */
    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(REDIS_KEY_PREFIX + conversationId);
    }
}
