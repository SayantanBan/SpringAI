package com.sayantan.mcpserver.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
//        return ChatClient.create(openAiChatModel);
        ChatOptions chatOptions = ChatOptions.builder()
                .model("gpt-4.1-mini")
                .maxTokens(1000)
                .temperature(0.8)
                .build();

        ChatClient.Builder chatClientBuilder = ChatClient.builder(openAiChatModel);
        return chatClientBuilder
                .defaultOptions(chatOptions)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        ChatClient.Builder chatClientBuilder = ChatClient.builder(ollamaChatModel);
        return chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor()).defaultSystem("""
                 You are an internal IT helpdesk assistant. Your role is to assist\s
                 employees with IT-related issues such as resetting passwords,\s
                 unlocking accounts, and answering questions related to IT policies.
                 If a user requests help with anything outside of these\s
                 responsibilities, respond politely and inform them that you are\s
                 only able to assist with IT support tasks within your defined scope.
                \s""").defaultUser("How can I help you?").build();
    }
}
