package com.xf.rag.controller;


import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankModel;
import com.alibaba.cloud.ai.document.DocumentWithScore;
import com.alibaba.cloud.ai.model.RerankRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AgentRagChatController
 *
 * @author 海言
 * @date 2026/5/8
 * @time 17:23
 * @Description rag对话控制器
 */
@RestController
@RequestMapping("/agentRag")
@Slf4j
public class AgentRagChatController {

    private final ChatClient chatClient;


    // 构造器注入 ChatClient.Builder 和 VectorStore
    public AgentRagChatController(ChatClient.Builder chatClientBuilder,
                                  VectorStore vectorStore,
                                  ChatMemory chatMemory,
                                  DashScopeRerankModel rerankModel) {

        // 🏆 架构核心：自研“双重漏斗”检索器
        DocumentRetriever dualRetriever = query -> {
            // 漏斗第一级：ES 粗排 (捞取 Top 20)
            List<Document> rawDocs = vectorStore.similaritySearch(
                    SearchRequest.builder().query(query.text()).topK(20).build()
            );
            log.info("ES 粗排完毕，捞出 {} 条切片，准备进入精排...", rawDocs.size());
            // 漏斗第二级：Rerank 精排 (浓缩至 Top 3)
            // 调用阿里 Rerank 模型进行交叉打分重排
            RerankRequest rerankRequest = new RerankRequest(query.text(), rawDocs);
            List<DocumentWithScore> resultScore = rerankModel.call(rerankRequest).getResults();
            List<Document> results = resultScore.stream().map(DocumentWithScore::getOutput).toList();
            log.info("精排完毕，已锁定最精准的 3 条切片！");
            return results;
        };
        // 装配 ChatClient
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个公司项目技术方案助手。请根据提供的知识库内容，准确、专业地回答员工的问题。如果在知识库中找不到答案，请诚实地说明。")
                .defaultAdvisors(
                        //核心逻辑1：注入精排后的 3 块文档切片，塞给大模型
                        new DocumentRetrievalAdvisor(dualRetriever),
                        //核心逻辑2：内存拦截器 (RAG 核心)。每次问答前，自动把上一轮的会话塞给大模型
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                // 注入工具能力
                .defaultToolNames("weatherFunction", "orderFunction")
                .build();
    }

    /**
     * RAG 知识检索与对话接口：实现带上下文和知识库的问答
     */
    @GetMapping("/chat")
    public String chatWithKnowledge(@RequestParam("chatId") String chatId,
                                    @RequestParam("prompt") String prompt) {

        long startTime = System.currentTimeMillis();
        log.info("接收到会话 [{}] 的咨询: {}", chatId, prompt);

        try {
            // 这就是 Spring AI 极其惊艳的流式/链式 API
            String response = chatClient.prompt()
                    .user(prompt)
                    // 绑定对话 ID，让 AI 知道这次对话属于哪个用户/窗口
                    .advisors(a -> a.param("chat_memory_conversation_id", chatId))
                    .call()
                    .content();

            log.info("会话 [{}] 思考完毕，耗时 {} ms", chatId, (System.currentTimeMillis() - startTime));
            return response;

        } catch (Exception e) {
            log.error("AI 思考时发生脑梗: {}", e.getMessage(), e);
            return "对不起，AI 助手暂时遇到了系统异常，请稍后再试。";
        }
    }
}
