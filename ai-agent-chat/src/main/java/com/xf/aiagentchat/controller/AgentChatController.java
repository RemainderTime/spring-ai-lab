package com.xf.aiagentchat.controller;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AgentChatController
 *
 * @author 海言
 * @date 2026/3/24
 * @time 14:29
 * @Description 智能体会话控制器
 */
@RestController
@RequestMapping("/ai/agent")
public class AgentChatController {


    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public AgentChatController(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        this.chatClient = builder
                .defaultToolNames("weatherFunction", "orderFunction")
                .build();
    }


    /**
     * 智能体上下文记忆会话（JVM内存存储版）
     *
     * @param chatId
     * @param message
     * @return
     */
    @GetMapping("/chat/memory")
    public String chat(
            @RequestParam String chatId,  // 模拟不同用户的独立记忆
            @RequestParam String message) {

        return chatClient.prompt()
                .user(message)
                // 👇 挂载记忆拦截器：传入存储引擎、当前会话 ID、滑动窗口大小
                .advisors(
                        MessageChatMemoryAdvisor.
                                builder(this.chatMemory)
                                .conversationId(chatId)
                                .order(10)
                                .build()
                )
                .call()
                .content();
    }
}
