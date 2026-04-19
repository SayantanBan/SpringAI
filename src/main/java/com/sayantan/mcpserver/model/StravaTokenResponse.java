package com.sayantan.mcpserver.model;

// Support Record for the API Response
public record StravaTokenResponse(
        String token_type,
        String access_token,
        String refresh_token,
        long expires_at,
        long expires_in
) {}