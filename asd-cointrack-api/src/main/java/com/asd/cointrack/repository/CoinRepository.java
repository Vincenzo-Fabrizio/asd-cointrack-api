package com.asd.cointrack.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.asd.cointrack.model.Coin;
import com.asd.cointrack.model.NumismaticRarity;
import com.asd.cointrack.model.OptionConservation;

public interface CoinRepository extends MongoRepository <Coin, String> {
    
    List <Coin> findByName(String name); 

    List <Coin> findByYear(int year);

    List <Coin> findByMaterial(String material);

    @Query("{ 'weight': { $gte: ?0, $lte: ?1 } }")
    List <Coin> findByWeight(double weight);

    @Query("{ 'diameter': { $gte: ?0, $lte: ?1 } }")
    List <Coin> findByDiameter(double diameter);

    @Query("{ 'height': { $gte: ?0, $lte: ?1 } }")
    List <Coin> findByHeight(double height);

    @Query("{ 'price': { $gte: ?0, $lte: ?1 } }")
    List <Coin> findByPriceBetween(double minPrice, double maxPrice);

    List <Coin> findByConservationObverse(OptionConservation conservationObverse);

    List <Coin> findByConservationReverse(OptionConservation conservationReverse);

    List <Coin> findByDegree(NumismaticRarity rarity);

    List <Coin> findByNote(String note);

    List <Coin> findByPhotoPathObverse(String photoPathObverse);

    List <Coin> findByPhotoPathReverse(String photoPathReverse);

}
