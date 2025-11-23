package com.asd.cointrack.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asd.cointrack.model.Coin;
import com.asd.cointrack.model.CoinCollection;
import com.asd.cointrack.repository.CoinCollectionRepository;
import com.asd.cointrack.repository.CoinRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoinCollectionService {

    private final CoinCollectionRepository collectionRepository;
    private final CoinRepository coinRepository;

    public List<CoinCollection> getAllCollections() {
        return collectionRepository.findAll();
    }

    public CoinCollection getCollectionById(String id) {
        return collectionRepository.findById(id).orElse(null);
    }

    public CoinCollection createCollection(CoinCollection collection) {
        return collectionRepository.save(collection);
    }

    public Page<Coin> getCoinsByCollection(String collectionId, Pageable pageable) {
        return coinRepository.findByCollectionId(collectionId, pageable);
    }
}

