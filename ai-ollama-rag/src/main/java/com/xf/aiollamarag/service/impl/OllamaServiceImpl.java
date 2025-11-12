package com.xf.aiollamarag.service.impl;

import com.xf.aiollamarag.service.OllamaService;
import jakarta.annotation.Resource;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

/**
 * OllamaServiceImpl
 *
 * @author 海言
 * @date 2025/11/12
 * @time 17:17
 * @Description
 */
@Service
public class OllamaServiceImpl implements OllamaService {

    @Resource
    private OllamaChatModel ollamaChatModel;



}
