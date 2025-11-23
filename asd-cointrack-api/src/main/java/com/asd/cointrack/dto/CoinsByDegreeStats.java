package com.asd.cointrack.dto;

import com.asd.cointrack.model.NumismaticRarity;

public record CoinsByDegreeStats(NumismaticRarity degree, long count) {
}

