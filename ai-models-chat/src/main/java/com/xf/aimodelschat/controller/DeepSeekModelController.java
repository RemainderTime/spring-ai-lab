package com.xf.aimodelschat.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * DeepSeekModelController
 *
 * @author 海言
 * @date 2025/11/18
 * @time 17:39
 * @Description DeepSeek大模型控制器（无赠送额度）
 */
@RestController
@RequestMapping("/ai/deepseek")
public class DeepSeekModelController {

    @Resource
    private DeepSeekChatModel chatModel;

    /**
     * deepseek 模型直接调用
     *
     * @return
     */
    @GetMapping("/call/chat")
    public String callChat(@RequestParam String message, @RequestParam(defaultValue = "deepseek-chat") String model) {
        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        DeepSeekChatOptions.builder()
                                .model(model)
                                .temperature(0.7)
                                .build()
                ));
        return response.getResult().getOutput().getText();
    }

    /**
     * deepseek 模型流式调用
     *
     * @return
     */
    @GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestParam String message, @RequestParam(defaultValue = "deepseek-chat") String model) {
        return chatModel.stream(
                        new Prompt(message, DeepSeekChatOptions.builder().model(model).build())
                )
                .map(chatResponse -> {
                    String text = chatResponse.getResults()
                            .stream()
                            .filter(r -> r.getOutput().getText() != null)
                            .map(r -> r.getOutput().getText())
                            .findFirst()
                            .orElse("");

                    return ServerSentEvent.<String>builder()
                            .data(text)
                            .build();
                })
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .data("[DONE]")
                                .build()
                ))
                .doOnError(Throwable::printStackTrace);
    }
}
