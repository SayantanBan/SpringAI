package com.sayantan.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record StravaToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_at") long expiresAt // Unix timestamp in seconds
) {
    /**
     * Checks if the token is expired or will expire in the next 5 minutes.
     * Use this to trigger the refresh logic before making an API call.
     */
    public boolean isExpired() {
        long nowInSeconds = Instant.now().getEpochSecond();
        return nowInSeconds >= (expiresAt - 300); // 5-minute safety buffer
    }
}