package com.xf.rag.memory;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * RedisChatMemory
 *
 * @author 海言
 * @date 2026/3/27
 * @time 15:47
 * @Description redis分布式记忆会话操作类
 */

@Slf4j
public class RedisChatMemory implements ChatMemory {

    private final StringRedisTemplate stringRedisTemplate;
    private final int maxMessages;
    private final long expireDays;
    private static final String KEY_PREFIX = "ai:agentRag:memory:";

    // 【核心架构设计】：我们自己定义的纯净数据结构，没有任何框架包袱！
    @Data
    public static class MessageDto {
        private String type;
        private String content;
        public MessageDto() {
        } // 必须有无参构造
        public MessageDto(String type, String content) {
            this.type = type;
            this.content = content;
        }
    }

    public RedisChatMemory(StringRedisTemplate stringRedisTemplate, int maxMessages, long expireDays) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.maxMessages = maxMessages;
        this.expireDays = expireDays;
    }

    @Override
    public void add(@NonNull String conversationId,@NonNull List<Message> messages) {
        String key = KEY_PREFIX + conversationId;
        List<Message> history = get(conversationId);
        List<Message> mutableHistory = new ArrayList<>();
        if (!history.isEmpty()) {
            mutableHistory.addAll(history);
        }
        if (!messages.isEmpty()) {
            mutableHistory.addAll(messages);
        }
        if (mutableHistory.size() > maxMessages) {
            mutableHistory = mutableHistory.subList(mutableHistory.size() - maxMessages, mutableHistory.size());
        }

        // 【降维打击 - 存入】：把复杂的 Message 剥离成我们自己干净的 DTO
        List<MessageDto> dtos = mutableHistory.stream()
                .map(m -> new MessageDto(
                        m.getMessageType().getValue(),
                        m.getText() != null ? m.getText() : ""
                ))
                .collect(Collectors.toList());

        // 像存普通业务数据一样存进去，极其稳健
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(dtos), expireDays, TimeUnit.DAYS);
    }

    @Override
    public List<Message> get(@NonNull String conversationId) {
        String key = KEY_PREFIX + conversationId;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);

        if (jsonStr == null || jsonStr.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 【降维打击 - 取出】：先解析成我们的 DTO
            List<MessageDto> dtos = JSON.parseArray(jsonStr, MessageDto.class);

            // 然后手动 new 出大模型需要的标准对象
            List<Message> messages = dtos.stream().map(dto -> {
                String type = dto.getType();
                String content = dto.getContent();
                if ("user".equalsIgnoreCase(type)) {
                    return new UserMessage(content);
                } else if ("assistant".equalsIgnoreCase(type)) {
                    return new AssistantMessage(content);
                } else if ("system".equalsIgnoreCase(type)) {
                    return new SystemMessage(content);
                }
                return new UserMessage(content); // 兜底
            }).collect(Collectors.toList());

            int fromIndex = Math.max(0, messages.size() - 500);
            return new ArrayList<>(messages.subList(fromIndex, messages.size()));

        } catch (Exception e) {
            log.warn("解析缓存异常，已清空脏数据: {}", e.getMessage());
            stringRedisTemplate.delete(key);
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(@NonNull String conversationId) {
        stringRedisTemplate.delete(KEY_PREFIX + conversationId);
    }
}
