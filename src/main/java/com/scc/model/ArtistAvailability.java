package com.scc.model;

public record ArtistAvailability(
        int availabilityId,
        int artistId,
        String dayOfWeek,
        String startTime,
        String endTime,
        boolean available
) {}