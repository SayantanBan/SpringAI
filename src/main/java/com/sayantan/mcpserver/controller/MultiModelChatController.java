package com.sayantan.mcpserver.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MultiModelChatController {


    private final ChatClient openAIChatClient;

    private final ChatClient ollamaChatClient;

    public MultiModelChatController(@Qualifier("openAiChatClient") ChatClient openAIChatClient, @Qualifier("ollamaChatClient") ChatClient ollamaChatClient) {
        this.openAIChatClient = openAIChatClient;
        this.ollamaChatClient = ollamaChatClient;
    }

    @GetMapping("/openai/chat")
    public String openAiChat(@RequestParam("message") String message){
//        return openAIChatClient.prompt(message).call().content();
        return openAIChatClient.prompt().system("""
                        You are an internal IT helpdesk assistant. Your role is to assist\s
                        employees with IT-related issues such as resetting passwords,\s
                        unlocking accounts, and answering questions related to IT policies.
                        If a user requests help with anything outside of these\s
                        responsibilities, respond politely and inform them that you are\s
                        only able to assist with IT support tasks within your defined scope.
                       \s""").user(message).call().content();

    }

    @GetMapping("/ollama/chat")
    public String ollamaChat(@RequestParam("message") String message) {
        return ollamaChatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }

}
