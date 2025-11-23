package com.asd.cointrack.dto;

public record CoinsSummaryStats(
        long totalCount,
        double totalPrice,
        double averagePrice,
        Integer minYear,
        Integer maxYear) {
}

