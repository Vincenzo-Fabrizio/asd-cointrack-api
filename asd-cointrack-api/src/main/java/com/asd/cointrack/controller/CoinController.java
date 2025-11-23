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

@Validated
@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;

    @GetMapping
    public Page<Coin> getAllCoins(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return coinService.getAllCoins(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coin> getCoinById(@PathVariable String id) {
        Coin coin = coinService.getCoinById(id);
        if (coin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coin);
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<CoinAuditInfo> getCoinAudit(@PathVariable String id) {
        CoinAuditInfo auditInfo = coinService.getCoinAuditInfo(id);
        if (auditInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(auditInfo);
    }

    @GetMapping("/search")
    public Page<Coin> searchCoins(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer year,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        return coinService.searchCoins(name, year, pageable);
    }

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

    @PostMapping
    public ResponseEntity<Coin> createCoin(@Valid @RequestBody Coin coin) {
        Coin created = coinService.createCoin(coin);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coin> updateCoin(@PathVariable String id, @Valid @RequestBody Coin coin) {
        Coin updated = coinService.updateCoin(id, coin);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoin(@PathVariable String id) {
        coinService.deleteCoin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/summary")
    public CoinsSummaryStats getSummaryStats() {
        return coinService.getSummaryStats();
    }

    @GetMapping("/stats/by-year")
    public List<CoinsByYearStats> getStatsByYear() {
        return coinService.getStatsByYear();
    }

    @GetMapping("/stats/by-degree")
    public List<CoinsByDegreeStats> getStatsByDegree() {
        return coinService.getStatsByDegree();
    }

    @GetMapping("/stats/by-material")
    public List<CoinsByMaterialStats> getStatsByMaterial() {
        return coinService.getStatsByMaterial();
    }

    @GetMapping("/stats/top-expensive")
    public List<Coin> getTopExpensiveCoins(@RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return coinService.getTopExpensiveCoins(limit);
    }
}
