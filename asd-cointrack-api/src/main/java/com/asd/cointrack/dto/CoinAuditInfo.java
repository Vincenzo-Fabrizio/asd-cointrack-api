package com.asd.cointrack.dto;

import java.time.Instant;

/**
 * DTO exposing audit metadata for a {@link com.asd.cointrack.model.Coin}.
 *
 * @param id        coin identifier
 * @param name      coin name
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 * @param createdBy creator identifier
 * @param updatedBy last modifier identifier
 */
public record CoinAuditInfo(
        String id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy) {
}
