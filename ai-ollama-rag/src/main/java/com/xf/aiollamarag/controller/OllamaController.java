package com.xf.aiollamarag.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OllamaController
 *
 * @author 海言
 * @date 2025/11/12
 * @time 17:16
 * @Description
 */
@RestController
@RequestMapping("/ai/ollama")
public class OllamaController {
    @Resource
    private OllamaChatModel ollamaChatModel;
    @Resource
    private PgVectorStore pgVectorStore;

    /**
     * ollama deepseek-r1:1.5b 直接应答
     * @param model
     * @param message
     * @return
     */
    @RequestMapping(value = "/generate", method = RequestMethod.GET)
    public ChatResponse generate(@RequestParam("model") String model, @RequestParam("message") String message) {
        return ollamaChatModel.call(new Prompt(message, OllamaOptions.builder()
                .model(model)
                .build()));
    }

    /**
     *  ollama deepseek-r1:1.5b 流式应答
     * @param model
     * @param message
     * @return
     */
    @GetMapping(value = "/generate_stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateStream(
            @RequestParam String model,
            @RequestParam String message) {

        return ollamaChatModel.stream(
                        new Prompt(message, OllamaOptions.builder().model(model).build())
                )
                .map(chatResponse -> {
                    String text = chatResponse.getResults().stream()
                            .map(r -> r.getOutput().getText())
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining());
                    return ServerSentEvent.<String>builder()
                            .data(text)
                            .build();
                });
    }


    /**
     *  ollama deepseek-r1:1.5b 带rag的直接应答
     * @param model
     * @param ragTag
     * @param message
     * @return
     */
    @RequestMapping(value = "/generateCallRag", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ChatResponse generateCallRag(@RequestParam("model") String model, @RequestParam("ragTag") String ragTag, @RequestParam("message") String message) {

        return ollamaChatModel.call(new Prompt(
                this.createSystemMessage(message, ragTag),
                OllamaOptions.builder()
                        .model(model)
                        .build()));
    }

    /**
     *  ollama deepseek-r1:1.5b 带rag的流式应答
     * @param model
     * @param ragTag
     * @param message
     * @return
     */
    @RequestMapping(value = "/generate_stream_rag", method = RequestMethod.GET)
    public Flux<ChatResponse> generateStreamRag(@RequestParam("model") String model, @RequestParam("ragTag") String ragTag, @RequestParam("message") String message) {


        return ollamaChatModel.stream(new Prompt(
                this.createSystemMessage(message, ragTag),
                OllamaOptions.builder()
                        .model(model)
                        .build()));
    }

    private Message createSystemMessage(String message, String ragTag) {
        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;
        SearchRequest request = SearchRequest.builder()
                .query(message)
                .topK(5)
                .filterExpression("knowledge == '" + ragTag + "'")
                .build();

        List<Document> documents = pgVectorStore.similaritySearch(request);

        String documentsCollectors = documents.stream().map(Document::getText).collect(Collectors.joining());

        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentsCollectors));

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(message));
        messages.add(ragMessage);
        return ragMessage;
    }
}
