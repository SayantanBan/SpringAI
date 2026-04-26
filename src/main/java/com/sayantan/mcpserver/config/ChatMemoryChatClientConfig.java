package com.sayantan.mcpserver.config;

import com.sayantan.mcpserver.advisor.TokenUsageAuditAdvisor;
import com.sayantan.mcpserver.rag.PIIMaskingDocumentPostProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
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
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor tokenUsgaeAdvisor = new TokenUsageAuditAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(loggerAdvisor, memoryAdvisor, tokenUsgaeAdvisor, retrievalAugmentationAdvisor)
                .build();
    }

    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore, OpenAiChatModel openAiChatModel) {
        return RetrievalAugmentationAdvisor.builder()
//                .queryTransformers(TranslationQueryTransformer.builder()
//                        .chatClientBuilder(ChatClient.builder(openAiChatModel).clone())
//                        .targetLanguage("english").build())
                .documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore)
                        .topK(3).similarityThreshold(0.5).build())
                .documentPostProcessors(PIIMaskingDocumentPostProcessor.builder())
                .build();
    }
}
