package com.xf.aiollamarag.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Description:
 * @ClassName: WebController
 * @Author: xiongfeng
 * @Date: 2025/11/13 22:57
 * @Version: 1.0
 */
@Controller
public class WebController {

	@GetMapping("/chat")
	public String chatPage() {
		return "chat"; // 对应 templates/chat.html
	}

}
