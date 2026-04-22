package com.sayantan.mcpserver;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class McpserverApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(McpserverApplication.class)
                .headless(false)
                .run(args);
    }

}
