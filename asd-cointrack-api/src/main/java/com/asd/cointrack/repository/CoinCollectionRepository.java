package com.asd.cointrack.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.asd.cointrack.model.CoinCollection;

public interface CoinCollectionRepository extends MongoRepository<CoinCollection, String> {
}

