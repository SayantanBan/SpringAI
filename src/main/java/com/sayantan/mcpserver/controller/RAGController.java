package com.sayantan.mcpserver.controller;

import com.sayantan.mcpserver.advisor.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api")
public class RAGController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/promptTemplates/systemPromptRandomDataTemplate.st")
    Resource promptTemplate;

    public RAGController(@Qualifier("chatMemoryChatClient") ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/random/chat")
    public ResponseEntity<String> chatMemory(@RequestParam("message") String message, @RequestHeader("username") String username) {
        SearchRequest searchRequest =
                SearchRequest.builder().query(message).topK(3).similarityThreshold(0.3).build();
        List<Document> similarDocs =  vectorStore.similaritySearch(searchRequest);

        if (similarDocs.isEmpty()) {
            return ResponseEntity.ok("I don't know");
        }

        String similarContext = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String answer = chatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec.text(promptTemplate)
                .param("documents", similarContext))
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(message)
                .call().content();
        return ResponseEntity.ok(answer);
    }
}
