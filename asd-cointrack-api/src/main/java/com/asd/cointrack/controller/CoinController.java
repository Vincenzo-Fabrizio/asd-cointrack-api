package com.asd.cointrack.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asd.cointrack.model.Coin;
import com.asd.cointrack.service.CoinService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "coinService is an injected Spring bean, not exposed outside")
public class CoinController {

    private final CoinService coinService;

    @GetMapping
    public List<Coin> getAllCoins() {
        return coinService.getAllCoins();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coin> getCoinById(@PathVariable String id) {
        Coin coin = coinService.getCoinById(id);
        if (coin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coin);
    }

    @GetMapping("/search")
    public List<Coin> searchCoins(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer year) {

        if (name != null) {
            return coinService.getCoinsByName(name);
        }

        if (year != null) {
            return coinService.getCoinsByYear(year);
        }

        return coinService.getAllCoins();
    }

    @PostMapping
    public ResponseEntity<Coin> createCoin(@RequestBody Coin coin) {
        Coin created = coinService.createCoin(coin);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coin> updateCoin(@PathVariable String id, @RequestBody Coin coin) {
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
}
