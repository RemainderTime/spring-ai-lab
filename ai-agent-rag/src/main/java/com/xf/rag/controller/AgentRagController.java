package com.xf.rag.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

/**
 * AgentRagController
 *
 * @author 海言
 * @date 2026/5/6
 * @time 16:24
 * @Description
 */
@RestController
@RequestMapping("/agentRag")
@Slf4j
public class AgentRagController {

    private final VectorStore vectorStore;

    public AgentRagController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * RAG 知识库进货接口：接收文件上传，执行切片并向量化入库
     * 适用于：.txt, .md 等纯文本文件
     */
    @PostMapping("/upload")
    public String uploadKnowledgeFile(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        String filename = file.getOriginalFilename();
        log.info("开始处理知识库文件上传任务, 文件名: {}", filename);

        if (file.isEmpty()) {
            return "上传失败：文件不能为空";
        }

        try {
            // 1. 文件加载 (Load)：巧妙利用 Spring 的 Resource 接口，避免在本地磁盘生成临时文件
            Resource resource = file.getResource();
            TextReader textReader = new TextReader(resource);

            // 注入核心元数据 (Metadata)。在后续的复杂查询中，我们可以用 metadata 来做精确的 SQL like 过滤
            textReader.getCustomMetadata().put("source_filename", filename);
            textReader.getCustomMetadata().put("upload_timestamp", System.currentTimeMillis());

            // 将文件内容读取为 Spring AI 的标准 Document 对象
            List<Document> rawDocs = textReader.get();
            log.info("文件读取完成，准备执行语义切片...");

            // 2. 文本切片 (Split)：维持黄金比例
            TokenTextSplitter splitter = new TokenTextSplitter(800, 800, 150, 10000, true, Collections.emptyList());
            List<Document> chunkedDocs = splitter.apply(rawDocs);
            log.info("文件切片完成，共切分为 {} 个数据块，准备调用大模型向量化并存入 ES...", chunkedDocs.size());

            // 3. 向量化与存储 (Embed & Store)：全自动批量处理
            vectorStore.add(chunkedDocs);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("知识库入库大功告成！文件名: {}, 耗时: {} ms", filename, costTime);

            // 注意：真实项目中这里应该返回你统一定义的 Result/Response 对象
            return String.format("文件 [%s] 导入成功！共生成 %d 个知识切片，耗时 %d ms。", filename, chunkedDocs.size(), costTime);

        } catch (Exception e) {
            log.error("处理知识库文件时发生严重异常: {}", e.getMessage(), e);
            return "导入失败，系统内部异常，请查看日志。";
        }
    }

}
