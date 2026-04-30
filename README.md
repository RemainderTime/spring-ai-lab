# Spring AI Lab 🚀

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.4-blue.svg)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)

欢迎来到 **Spring AI Lab**！本项目是一个基于 Spring Boot 3 和 Spring AI 的实战探索与最佳实践集合。涵盖了从基础的大模型对话、RAG（检索增强生成）、到进阶的 AI Agent 智能体以及前沿的 MCP（Model Context Protocol）协议的完整应用示例。

项目中各模块独立且职责清晰，旨在帮助开发者快速掌握并在实际业务场景中落地 Spring AI 技术栈。

---

## 📦 模块概览

本项目采用 Maven 多模块架构，主要包含以下核心子模块：

### 1. `ai-models-chat` (基础对话与多模型接入)
- **简介**：展示如何使用 Spring AI 接入业界主流的各类大语言模型（如 OpenAI、阿里云百炼、Ollama 等），实现基础的文本生成、流式对话输出、Prompt 模板管理等核心基础能力。
- **核心技术**：`ChatClient`、`Prompt Templates`、`Streaming API`。
- **📖 教程博客**：[点击这里填写您的博客教程地址](#)

### 2. `ai-ollama-rag` (本地知识库与 RAG 检索增强)
- **简介**：基于本地私有化部署的大模型（Ollama）和向量数据库（Vector Store），实现文档解析（PDF/TXT 等）、Embedding 向量化存储与相似度检索，最终构建完整的 RAG（检索增强生成）问答链路，解决大模型幻觉与私有数据问题。
- **核心技术**：`Ollama`、`Document Readers`、`Vector Databases`、`RAG`。
- **📖 教程博客**：[点击这里填写您的博客教程地址](#)

### 3. `ai-agent-chat` (AI 智能体与函数调用)
- **简介**：探索 AI Agent 的核心能力——**Function Calling（函数调用/工具调用）**。通过赋予大模型调用本地 Java 方法的能力，使其能够获取实时业务数据或执行实际动作（如查天气、操作数据库、调用第三方接口等），完成复杂任务的编排与规划。
- **核心技术**：`Function Calling`、`@Tool`、`Agent Planner`。
- **📖 教程博客**：[点击这里填写您的博客教程地址](#)

### 4. `ai-mcp-server` (MCP 服务端实现)
- **简介**：Model Context Protocol (MCP) 服务端标准实现示例。将本地系统资源、业务数据库或特定 API 统一封装为标准的 MCP 服务，安全地暴露给外部的 AI Agent 客户端调用，实现 AI 能力边界的标准扩展。
- **核心技术**：`Model Context Protocol (MCP)`、`Server-side Integration`。
- **📖 教程博客**：[点击这里填写您的博客教程地址](#)

### 5. `ai-mcp-client` (MCP 客户端接入)
- **简介**：演示如何作为 MCP 客户端，连接到一个或多个 MCP Server，动态发现并调用服务端提供的 Tool（工具）和 Resource（资源），实现跨进程、跨网络的高级 Agent 协作编排体系。
- **核心技术**：`MCP Client`、`Dynamic Tool Discovery`。
- **📖 教程博客**：[点击这里填写您的博客教程地址](#)

---

## 🛠️ 环境准备

- **JDK**: Java 17 或以上版本
- **构建工具**: Maven 3.8+
- **框架版本**:
  - Spring Boot `3.3.3`
  - Spring AI `1.1.4`
  - Spring Cloud Alibaba `2023.0.3.4`
- **依赖组件**:
  - Nacos (配置中心/服务发现，由 Spring Cloud 模块引入)
  - Redis (缓存与部分向量数据支撑)
  - Ollama (建议本地安装，用于低成本测试本地 RAG 和对话模型)

## 🚀 快速启动

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/spring-ai-lab.git
   cd spring-ai-lab
   ```

2. **配置模型密钥 / 服务地址**
   在各子模块的 `application.yml` 或 Nacos 配置中心中，配置相应的模型 API Key 或者本地 Ollama 服务的访问地址：
   ```yaml
   spring:
     ai:
       openai:
         api-key: sk-xxxxxx
         base-url: https://api.openai.com
   ```

3. **运行测试模块**
   找到对应需求模块的主启动类（如 `AiModelsChatApplication`），直接在 IDE 中运行，或使用 Maven 命令启动，结合接口调用工具（Postman/Apifox）进行测试。

---

## 🤝 参与贡献

欢迎对本开源实验室提出改进建议！如果您对 Spring AI 的落地有更多好的想法或最佳实践，欢迎提交 Pull Request 或者 Issue 交流讨论。

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。
