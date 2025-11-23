package com.asd.cointrack.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asd.cointrack.dto.CoinAuditInfo;
import com.asd.cointrack.dto.CoinsByDegreeStats;
import com.asd.cointrack.dto.CoinsByYearStats;
import com.asd.cointrack.dto.CoinsByMaterialStats;
import com.asd.cointrack.dto.CoinsSummaryStats;
import com.asd.cointrack.model.Coin;
import com.asd.cointrack.model.NumismaticRarity;
import com.asd.cointrack.model.OptionConservation;
import com.asd.cointrack.service.CoinService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/**
 * REST controller that exposes CRUD, search, statistics and audit endpoints for
 * the {@link Coin} resource.
 * <p>
 * All endpoints are rooted under {@code /api/coins} and return either paginated
 * results or typed DTOs to support rich client-side usage.
 */
@Validated
@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;

    /**
     * Returns a paginated and sortable list of all coins.
     *
     * @param pageable pagination and sorting information
     * @return page of coins
     */
    @GetMapping
    public Page<Coin> getAllCoins(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return coinService.getAllCoins(pageable);
    }

    /**
     * Returns details of a single coin identified by its id.
     *
     * @param id coin identifier
     * @return 200 with coin body or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Coin> getCoinById(@PathVariable String id) {
        Coin coin = coinService.getCoinById(id);
        if (coin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coin);
    }

    /**
     * Returns audit information (creation and last modification metadata) for a
     * single coin.
     *
     * @param id coin identifier
     * @return 200 with audit info or 404 if the coin does not exist
     */
    @GetMapping("/{id}/audit")
    public ResponseEntity<CoinAuditInfo> getCoinAudit(@PathVariable String id) {
        CoinAuditInfo auditInfo = coinService.getCoinAuditInfo(id);
        if (auditInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(auditInfo);
    }

    /**
     * Performs a basic search by name or year, returning a paginated list of
     * coins.
     *
     * @param name     optional exact name filter
     * @param year     optional year filter
     * @param pageable pagination and sorting information
     * @return page of coins matching the criteria
     */
    @GetMapping("/search")
    public Page<Coin> searchCoins(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer year,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        return coinService.searchCoins(name, year, pageable);
    }

    /**
     * Performs an advanced search combining textual, enum-based and numeric range
     * filters, returning a paginated list of coins.
     *
     * @param name                   optional case-insensitive name fragment
     * @param material               optional case-insensitive material fragment
     * @param degree                 optional single rarity filter
     * @param degreesIn              optional list of rarities to include
     * @param conservationObverseIn  optional list of obverse conservation levels
     * @param minYear                optional minimum minting year (inclusive)
     * @param maxYear                optional maximum minting year (inclusive)
     * @param minPrice               optional minimum price (inclusive)
     * @param maxPrice               optional maximum price (inclusive)
     * @param pageable               pagination and sorting information
     * @return page of coins matching the advanced criteria
     */
    @GetMapping("/advanced-search")
    public Page<Coin> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) NumismaticRarity degree,
            @RequestParam(required = false, name = "degreeIn") List<NumismaticRarity> degreesIn,
            @RequestParam(required = false, name = "conservationObverseIn") List<OptionConservation> conservationObverseIn,
            @RequestParam(required = false) @Min(0) Integer minYear,
            @RequestParam(required = false) @Min(0) Integer maxYear,
            @RequestParam(required = false) @DecimalMin("0.0") Double minPrice,
            @RequestParam(required = false) @DecimalMin("0.0") Double maxPrice,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        return coinService.advancedSearch(name, material, degree, degreesIn, conservationObverseIn,
                minYear, maxYear, minPrice, maxPrice, pageable);
    }

    /**
     * Creates a new coin based on the validated request body.
     *
     * @param coin coin payload to create
     * @return 201 with the created coin body
     */
    @PostMapping
    public ResponseEntity<Coin> createCoin(@Valid @RequestBody Coin coin) {
        Coin created = coinService.createCoin(coin);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing coin replacing its fields with the payload provided.
     *
     * @param id   coin identifier
     * @param coin validated coin payload
     * @return 200 with updated coin or 404 if the coin does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<Coin> updateCoin(@PathVariable String id, @Valid @RequestBody Coin coin) {
        Coin updated = coinService.updateCoin(id, coin);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a coin identified by its id.
     *
     * @param id coin identifier
     * @return 204 even if the coin does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoin(@PathVariable String id) {
        coinService.deleteCoin(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns aggregated summary statistics across all coins.
     *
     * @return summary statistics DTO
     */
    @GetMapping("/stats/summary")
    public CoinsSummaryStats getSummaryStats() {
        return coinService.getSummaryStats();
    }

    /**
     * Returns the number of coins grouped by year.
     *
     * @return list of year/count pairs
     */
    @GetMapping("/stats/by-year")
    public List<CoinsByYearStats> getStatsByYear() {
        return coinService.getStatsByYear();
    }

    /**
     * Returns the number of coins grouped by numismatic rarity.
     *
     * @return list of rarity/count pairs
     */
    @GetMapping("/stats/by-degree")
    public List<CoinsByDegreeStats> getStatsByDegree() {
        return coinService.getStatsByDegree();
    }

    /**
     * Returns the number of coins grouped by material.
     *
     * @return list of material/count pairs
     */
    @GetMapping("/stats/by-material")
    public List<CoinsByMaterialStats> getStatsByMaterial() {
        return coinService.getStatsByMaterial();
    }

    /**
     * Returns the most expensive coins, ordered by price in descending order.
     *
     * @param limit maximum number of coins to return (1-100)
     * @return list of the most expensive coins
     */
    @GetMapping("/stats/top-expensive")
    public List<Coin> getTopExpensiveCoins(@RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return coinService.getTopExpensiveCoins(limit);
    }
}
