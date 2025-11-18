package com.xf.aimodelschat.controller;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * GLMModelController
 *
 * @author 海言
 * @date 2025/11/18
 * @time 17:03
 * @Description GLM模型控制器(新用户赠送2千万tokens)
 */
@RestController
@RequestMapping("/ai/glm")
public class GLMModelController {


    @Resource
    private ZhiPuAiChatModel chatModel;

    /**
     * chatgpt 模型直接调用
     *
     * @return
     */
    @GetMapping("/call/chat")
    public String callChat(@RequestParam String message, @RequestParam(defaultValue = "glm-4.6") String model) {
        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        ZhiPuAiChatOptions.builder()
                                .model(model)
                                .temperature(0.7)
                                .build()
                ));
        return response.getResult().getOutput().getText();
    }

    /**
     * chatgpt 模型流式调用
     *
     * @return
     */
    @GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestParam String message, @RequestParam(defaultValue = "glm-4.6") String model) {
        return chatModel.stream(
                        new Prompt(message, ZhiPuAiChatOptions.builder().model(model).build())
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
