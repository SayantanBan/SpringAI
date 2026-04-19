package com.sayantan.mcpserver.service;

import com.sayantan.mcpserver.auth.StravaAuthService;
import com.sayantan.mcpserver.auth.StravaAuthService.NotAuthenticatedException;
import com.sayantan.mcpserver.model.ActivityStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Service
@Slf4j
public class StravaMcpService {

    private final WebClient webClient;
    private final StravaAuthService authService;

    public StravaMcpService(WebClient.Builder builder, StravaAuthService authService) {
        this.webClient = builder.baseUrl("https://www.strava.com/api/v3").build();
        this.authService = authService;
    }

    @McpTool(description = "Retrieve running stats with heart rate, pace, and speed")
    public String getRunningStats() {
        try {
            List<ActivityStats> stats = fetchStatsFromStrava();

            if (stats.isEmpty()) {
                return "No recent activities found on your Strava account.";
            }

            // Format as readable text for Claude
            StringBuilder sb = new StringBuilder("Here are your recent Strava activities:\n\n");
            for (int i = 0; i < stats.size(); i++) {
                ActivityStats a = stats.get(i);
                sb.append(String.format(
                        "%d. %s\n   Distance: %.2f km | Duration: %d min | " +
                                "Pace: %.2f min/km | Speed: %.2f km/h | Avg HR: %.0f bpm\n\n",
                        i + 1,
                        a.name(),
                        a.distanceKm(),
                        a.movingTimeMinutes(),   // was durationMinutes()
                        a.paceMinPerKm(),        // was pace()
                        a.maxSpeedKmh(),         // was speedKmh()
                        a.averageHeartrate()     // was avgHeartRate()
                ));
            }
            return sb.toString();

        } catch (NotAuthenticatedException e) {
            // Return the auth URL as a clear message — Claude will show it to the user
            return e.getMessage();
        } catch (Exception e) {
            log.error("Error fetching Strava stats", e);
            return "Error fetching Strava data: " + e.getMessage();
        }
    }

    private List<ActivityStats> fetchStatsFromStrava() {
        String token = authService.getValidToken();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/athlete/activities")
                        .queryParam("per_page", 10)
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .map(this::mapToActivityStats)
                .collectList()
                .block();
    }

    private ActivityStats mapToActivityStats(JsonNode node) {
        double distanceMeters = node.path("distance").asDouble();
        int movingTimeSeconds = node.path("moving_time").asInt();
        double avgSpeedMps = node.path("average_speed").asDouble();

        double distanceKm = distanceMeters / 1000.0;
        int durationMinutes = movingTimeSeconds / 60;
        double pace = (avgSpeedMps > 0) ? (16.6667 / avgSpeedMps) : 0;
        double speedKmh = avgSpeedMps * 3.6;

        return new ActivityStats(
                node.path("name").asText(),
                Math.round(distanceKm * 100.0) / 100.0,
                durationMinutes,           // maps to movingTimeMinutes
                node.path("average_heartrate").asDouble(),  // maps to averageHeartrate
                Math.round(pace * 100.0) / 100.0,          // maps to paceMinPerKm
                Math.round(speedKmh * 100.0) / 100.0       // maps to maxSpeedKmh
        );
    }
}