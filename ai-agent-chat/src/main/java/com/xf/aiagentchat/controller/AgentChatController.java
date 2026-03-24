package com.xf.aiagentchat.controller;


import jakarta.annotation.Resource;
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


    @Resource
    private DeepSeekChatModel chatModel;


    /**
     * deepseek 模型直接输出并实现tool function 能力
     *
     * @return
     */
    @GetMapping("/call/toolFunction/chat")
    public String toolFunctionCallChat(@RequestParam String message, @RequestParam(defaultValue = "deepseek-chat") String model) {
        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        DeepSeekChatOptions.builder()
                                .model(model)
                                .toolNames("weatherFunction", "orderFunction") //指定天气/订单 tool function
                                .build()
                ));
        return response.getResult().getOutput().getText();
    }
}
