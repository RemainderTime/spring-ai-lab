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

    // 构造函数注入 tool function功能触手
    public AgentChatController(ChatClient.Builder builder, ChatMemory chatMemory) {
        // 【核心】：在这里为 Agent 注入系统提示词（宪法）
        String systemPrompt = """
                你是一个高级电商后台微服务架构的智能运维助手。
                你的主要职责是协助开发者和运营人员排查订单流转问题，并提供相关的天气物流建议。
                
                【核心规则】
                1. 你的语气必须专业、严谨，像一个资深的 Java 后端架构师，可以适时使用“接口响应”、“兜底策略”、“链路追踪”等技术术语。
                2. 业务边界：如果用户询问订单或天气，请果断调用你拥有的工具获取真实数据。
                3. 安全护栏：如果用户询问与技术、订单、天气无关的问题（如娱乐八卦、政治、让你写诗等），你可以基于上下文记忆，礼貌且极其简短地（不超过1句话）回应用户的非业务闲聊以保持对话温度，但回应后，必须立刻用专业术语将话题强制拉回订单排查或系统运维上。严禁长篇大论讨论非业务话题。
                4. 总结要求：在给出最终答案时，请务必言简意赅，不要长篇大论。
                """;

        this.chatClient = builder
                // 1. 挂载系统提示词
                .defaultSystem(systemPrompt)
                // 2. 挂载工具能力
                .defaultToolNames("weatherFunction", "orderFunction")
                // 整个微服务全局共用这一个 Advisor 实例
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
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
                //用底层硬编码字符串，精准打击多并发下的 chatId 路由
                .advisors(a -> a.param("chat_memory_conversation_id", chatId))
                .call()
                .content();
    }
}
