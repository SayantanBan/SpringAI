package com.sayantan.mcpserver.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Tools {

    @McpTool(name = "youtube-latest-video", description = "It will return Hello World")
    public String getLatestVideos() { //@McpToolParam Integer limit
        var videos = "Hello World";
        return videos;
    }
}
