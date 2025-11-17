package com.xf.aimodelschat.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

/**
 * ClaudeModelController
 *
 * @author 海言
 * @date 2025/11/17
 * @time 15:16
 * @Description claude 模型控制器(没有免费额度，需要购买，安全性能好)
 */
@RestController
@RequestMapping("/ai/claude")
public class ClaudeModelController {

    @Resource
    private AnthropicChatModel chatModel;

    /**
     * claude 模型直接调用
     * @return
     */
    @GetMapping("/call/chat")
    public ChatResponse callChat(@RequestParam String message, @RequestParam(defaultValue = "claude-3-7-sonnet-latest")String model) {
        return chatModel.call(
                new Prompt(
                        message,
                        AnthropicChatOptions.builder()
                                .model(model)
                                .temperature(0.7)
                                .build()
                ));
    }

    /**
     * claude 模型流式调用
     * @return
     */
    @GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> streamChat(@RequestParam String message, @RequestParam(defaultValue = "claude-3-7-sonnet-latest")String model) {
        return chatModel.stream(
                new Prompt(
                        message,
                        AnthropicChatOptions.builder()
                                .model(model)
                                .temperature(0.7)
                                .build()
                ));
    }

}
