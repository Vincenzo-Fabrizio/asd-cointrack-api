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

/**
 * Service layer for managing {@link CoinCollection} entities and querying coins
 * by collection.
 */
@Service
@RequiredArgsConstructor
public class CoinCollectionService {

    private final CoinCollectionRepository collectionRepository;
    private final CoinRepository coinRepository;

    /**
     * Returns all collections.
     *
     * @return list of collections
     */
    public List<CoinCollection> getAllCollections() {
        return collectionRepository.findAll();
    }

    /**
     * Returns a collection by id.
     *
     * @param id collection identifier
     * @return collection or {@code null} if not found
     */
    public CoinCollection getCollectionById(String id) {
        return collectionRepository.findById(id).orElse(null);
    }

    /**
     * Creates a new collection.
     *
     * @param collection collection to persist
     * @return persisted collection
     */
    public CoinCollection createCollection(CoinCollection collection) {
        return collectionRepository.save(collection);
    }

    /**
     * Returns coins associated with the given collection in paginated form.
     *
     * @param collectionId collection identifier
     * @param pageable     pagination and sorting information
     * @return page of coins
     */
    public Page<Coin> getCoinsByCollection(String collectionId, Pageable pageable) {
        return coinRepository.findByCollectionId(collectionId, pageable);
    }
}
