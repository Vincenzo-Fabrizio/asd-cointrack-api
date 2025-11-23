package com.asd.cointrack.dto;

/**
 * DTO representing the number of coins for a specific material.
 *
 * @param material material name
 * @param count    number of coins using that material
 */
public record CoinsByMaterialStats(String material, long count) {
}
