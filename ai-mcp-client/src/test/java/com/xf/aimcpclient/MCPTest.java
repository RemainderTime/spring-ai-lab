package com.xf.aimcpclient;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description:
 * @ClassName: MCPTest
 * @Author: xiongfeng
 * @Date: 2025/11/26 21:50
 * @Version: 1.0
 */
@Slf4j
@SpringBootTest
public class MCPTest {

	@Resource
	private ChatClient.Builder chatClientBuilder;

	@Autowired
	private ToolCallbackProvider tools;

	@Test
	public void test_tool() {
		String userInput = "有哪些工具可以使用";
		var chatClient = chatClientBuilder
				.defaultToolCallbacks(tools)
				.defaultOptions(ZhiPuAiChatOptions.builder()
						.model("glm-4.6")
						.build())
				.build();

		System.out.println("\n>>> QUESTION: " + userInput);
		System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
	}

	@Test
	public void test_workflow() {
		String userInput = "获取电脑配置";
		userInput = "在 /Users/xiongfeng/Desktop 文件夹下，创建 电脑.txt  并写入内容我是大花猫";

		var chatClient = chatClientBuilder
				.defaultToolCallbacks(tools)
				.defaultOptions(ZhiPuAiChatOptions.builder()
						.model("glm-4.6")
						.build())
				.build();

		System.out.println("\n>>> QUESTION: " + userInput);
		System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
	}

}
