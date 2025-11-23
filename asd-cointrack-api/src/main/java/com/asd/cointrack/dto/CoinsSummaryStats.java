package com.asd.cointrack.dto;

/**
 * DTO representing aggregated summary statistics for all coins.
 *
 * @param totalCount   total number of coins
 * @param totalPrice   sum of prices of all coins
 * @param averagePrice average price of a coin
 * @param minYear      minimum minting year across all coins (nullable)
 * @param maxYear      maximum minting year across all coins (nullable)
 */
public record CoinsSummaryStats(
        long totalCount,
        double totalPrice,
        double averagePrice,
        Integer minYear,
        Integer maxYear) {
}
