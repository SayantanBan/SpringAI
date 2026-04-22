package com.sayantan.mcpserver.controller;

import com.sayantan.mcpserver.advisor.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api")
//@RequiredArgsConstructor
public class ChatMemoryController {

    private final ChatClient chatClient;

    public ChatMemoryController(@Qualifier("chatMemoryChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat-memory")
    public ResponseEntity<String> chatMemory(@RequestParam("message") String message, @RequestHeader("username") String username) {
        return ResponseEntity.ok(chatClient
                .prompt()
                .advisors(new TokenUsageAuditAdvisor())
                .user(message)
                        .advisors(advisorSpec ->
                                advisorSpec.param(CONVERSATION_ID, username))
                .call()
                .content());
    }
}
