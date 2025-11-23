package com.asd.cointrack.dto;

import com.asd.cointrack.model.NumismaticRarity;

/**
 * DTO representing the number of coins for a specific numismatic rarity.
 *
 * @param degree rarity level
 * @param count  number of coins with that rarity
 */
public record CoinsByDegreeStats(NumismaticRarity degree, long count) {
}
