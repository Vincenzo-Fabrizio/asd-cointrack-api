package com.asd.cointrack.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asd.cointrack.model.Coin;
import com.asd.cointrack.model.CoinCollection;
import com.asd.cointrack.service.CoinCollectionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CoinCollectionController {

    private final CoinCollectionService collectionService;

    @GetMapping
    public List<CoinCollection> getAllCollections() {
        return collectionService.getAllCollections();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoinCollection> getCollectionById(@PathVariable String id) {
        CoinCollection collection = collectionService.getCollectionById(id);
        if (collection == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(collection);
    }

    @PostMapping
    public ResponseEntity<CoinCollection> createCollection(@Valid @RequestBody CoinCollection collection) {
        CoinCollection created = collectionService.createCollection(collection);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/coins")
    public ResponseEntity<Page<Coin>> getCoinsByCollection(
            @PathVariable String id,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        CoinCollection collection = collectionService.getCollectionById(id);
        if (collection == null) {
            return ResponseEntity.notFound().build();
        }

        Page<Coin> coins = collectionService.getCoinsByCollection(id, pageable);
        return ResponseEntity.ok(coins);
    }
}

