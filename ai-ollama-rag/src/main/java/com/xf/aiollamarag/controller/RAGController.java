package com.xf.aiollamarag.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * RAGController
 *
 * @author 海言
 * @date 2025/11/12
 * @time 17:27
 * @Description 知识库相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai/ollama/rag")
public class RAGController {

    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource(name = "ollamaPgVectorStore")
    private PgVectorStore pgVectorStore;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 查询 rag 标签列表
     *
     * @return
     */
    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.GET)
    public List<String> queryRagTagList() {
        List<String> range = redisTemplate.opsForList().range("ai:rag:tags", 0, -1);
        return range;
    }

    /**
     * 上传知识库文件，并传入标签
     *
     * @param ragTag
     * @param files
     * @return
     */
    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    public String uploadFile(@RequestParam String ragTag, @RequestParam("file") List<MultipartFile> files) {
        log.info("上传知识库开始 {}", ragTag);
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = documentReader.get();
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // 添加知识库标签
            documents.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));

            pgVectorStore.accept(documentSplitterList);

            // 添加知识库记录
            List<String> elements = redisTemplate.opsForList().range("ai:arg:tags", 0, -1);
            assert elements != null;
            if (!elements.contains(ragTag)) {
                redisTemplate.opsForList().leftPush("ai:arg:tags", ragTag);
            }
        }
        log.info("上传知识库完成 {}", ragTag);
        return "success";
    }

    private String extractProjectName(String repoUrl) {
        String[] parts = repoUrl.split("/");
        String projectNameWithGit = parts[parts.length - 1];
        return projectNameWithGit.replace(".git", "");
    }


}
