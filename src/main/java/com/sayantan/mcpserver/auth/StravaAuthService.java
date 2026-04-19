package com.sayantan.mcpserver.auth;

import com.sayantan.mcpserver.model.StravaToken;
import com.sayantan.mcpserver.model.StravaTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
public class StravaAuthService {

    @Value("${strava.client-id}") private String clientId;
    @Value("${strava.client-secret}") private String clientSecret;

    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final Path TOKEN_PATH = Paths.get(
            System.getProperty("user.home"), ".mcp-strava", "token.json"
    );

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public StravaAuthService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns the authorization URL for the user to visit in their browser.
     * Does NOT open a browser or block.
     */
    public String getAuthorizationUrl() {
        return "https://www.strava.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + REDIRECT_URI
                + "&scope=activity:read_all,profile:read_all"
                + "&approval_prompt=auto";
    }

    /**
     * Returns a valid access token, or throws a clear exception
     * telling the user to authenticate first. Never blocks.
     */
    public String getValidToken() {
        // No token file — tell the user to authenticate, don't block
        if (!Files.exists(TOKEN_PATH)) {
            throw new NotAuthenticatedException(
                    "Not authenticated with Strava. Please visit: " +
                            "http://localhost:8080/auth/strava — authorize the app, then try again."
            );
        }

        try {
            StravaToken storedToken = objectMapper.readValue(TOKEN_PATH.toFile(), StravaToken.class);

            if (storedToken.isExpired()) {
                log.info("Token expired, refreshing...");
                return refreshAndSave(storedToken);
            }

            return storedToken.accessToken();

        } catch (NotAuthenticatedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error reading token, clearing and requesting re-auth", e);
            // Delete corrupt token file so next call prompts fresh auth
            try { Files.deleteIfExists(TOKEN_PATH); } catch (IOException ignored) {}
            throw new NotAuthenticatedException(
                    "Strava token is invalid or corrupted. Please re-authenticate at: " +
                            "http://localhost:8080/auth/strava"
            );
        }
    }

    public String refreshAndSave(StravaToken oldToken) {
        log.info("Refreshing Strava token...");
        StravaTokenResponse response = webClient.post()
                .uri("https://www.strava.com/oauth/token")
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "grant_type", "refresh_token",
                        "refresh_token", oldToken.refreshToken()
                ))
                .retrieve()
                .bodyToMono(StravaTokenResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Empty response when refreshing Strava token");
        }

        StravaToken newToken = new StravaToken(
                response.access_token(),
                response.refresh_token(),
                response.expires_at()
        );
        saveToken(newToken);
        return newToken.accessToken();
    }

    public void exchangeCodeAndSave(String code) {
        log.info("Exchanging authorization code for token...");
        StravaTokenResponse response = webClient.post()
                .uri("https://www.strava.com/oauth/token")
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code,
                        "grant_type", "authorization_code"
                ))
                .retrieve()
                .bodyToMono(StravaTokenResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Empty response when exchanging Strava auth code");
        }

        saveToken(new StravaToken(
                response.access_token(),
                response.refresh_token(),
                response.expires_at()
        ));
        log.info("Strava token saved successfully.");
    }

    private void saveToken(StravaToken token) {
        try {
            Files.createDirectories(TOKEN_PATH.getParent());
            objectMapper.writeValue(TOKEN_PATH.toFile(), token);
            log.info("Token saved to: {}", TOKEN_PATH.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not save Strava token to disk", e);
        }
    }

    // ── Custom exception so MCP tools can catch it cleanly ───────────
    public static class NotAuthenticatedException extends RuntimeException {
        public NotAuthenticatedException(String message) { super(message); }
    }

    // ── OAuth callback + auth initiation controller ───────────────────
    @RestController
    public class StravaAuthController {

        /** Redirect user to Strava's authorization page */
        @GetMapping("/auth/strava")
        public void initiateAuth(jakarta.servlet.http.HttpServletResponse response)
                throws IOException {
            response.sendRedirect(getAuthorizationUrl());
        }

        /** Strava redirects here after the user authorizes */
        @GetMapping("/callback")
        public String handleCallback(@RequestParam String code,
                                     @RequestParam(required = false) String error) {
            if (error != null) {
                log.error("Strava auth error: {}", error);
                return "Authorization failed: " + error + ". Please try again at /auth/strava";
            }
            exchangeCodeAndSave(code);
            return "Successfully connected to Strava! You can close this tab and return to Claude.";
        }
    }
}