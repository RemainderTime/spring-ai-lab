package com.xf.aimodelschat.controller;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ChatGPTController
 *
 * @author 海言
 * @date 2025/11/18
 * @time 13:48
 * @Description ChatGPT模型控制器
 */
@RestController
@RequestMapping("/ai/chatgpt")
public class ChatGPTModelController {

    @Autowired
    private OpenAiChatModel chatModel;

    /**
     * chatgpt 模型直接调用
     *
     * @return
     */
    @GetMapping("/call/chat")
    public String callChat(@RequestParam String message, @RequestParam(defaultValue = "gpt-4o-mini") String model) {
        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        OpenAiChatOptions.builder()
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
    public Flux<ServerSentEvent<String>> streamChat(@RequestParam String message, @RequestParam(defaultValue = "gpt-4o-mini") String model) {
        return chatModel.stream(
                        new Prompt(message, OpenAiChatOptions.builder().model(model).build())
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

    /**
     * chatgpt 模型直接调用(图片内容)
     * 只支持 GPT_4_O 或 GPT_4_TURBO 模型
     *
     * @return
     */
    @PostMapping("/call/chatImg")
    public String callChatImg(@RequestParam String message,
                              @RequestParam(defaultValue = "gpt-4o") String model,
                              @RequestParam MultipartFile imageFile) {
        // 读取图片
        byte[] imageBytes = null;
        try {
            imageBytes = imageFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Resource imageResource = new ByteArrayResource(imageBytes);
        // 创建消息列表
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        // 添加文本消息
        messages.add(new UserMessage(message));
        // 添加图片消息
        messages.add(new UserMessage(imageResource));
        // 调用模型
        ChatResponse response = chatModel.call(
                new Prompt(
                        messages,
                        OpenAiChatOptions.builder()
                                .model(model)
                                .build()
                )
        );
        return response.getResult().getOutput().getText();
    }

}
