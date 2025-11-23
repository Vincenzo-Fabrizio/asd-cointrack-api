package com.asd.cointrack.dto;

import java.time.Instant;

public record CoinAuditInfo(
        String id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy) {
}

