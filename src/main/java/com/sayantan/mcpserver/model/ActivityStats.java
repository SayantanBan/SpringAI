package com.sayantan.mcpserver.model;

public record ActivityStats(
        String name,
        double distanceKm,
        int movingTimeMinutes,
        double averageHeartrate,
        double paceMinPerKm,
        double maxSpeedKmh
) {}
