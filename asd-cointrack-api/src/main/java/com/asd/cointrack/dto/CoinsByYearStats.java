package com.asd.cointrack.dto;

/**
 * DTO representing the number of coins minted in a given year.
 *
 * @param year  minting year
 * @param count number of coins for that year
 */
public record CoinsByYearStats(int year, long count) {
}
