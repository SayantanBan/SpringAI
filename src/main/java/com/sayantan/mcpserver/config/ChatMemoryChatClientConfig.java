package com.sayantan.mcpserver.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryChatClientConfig {

//    @Bean
//    ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
//        return MessageWindowChatMemory.builder().maxMessages(10)
//                .chatMemoryRepository(jdbcChatMemoryRepository).build();
//    }

    @Bean("chatMemoryChatClient")
    public ChatClient chatClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(loggerAdvisor, memoryAdvisor)
                .build();
    }
}
