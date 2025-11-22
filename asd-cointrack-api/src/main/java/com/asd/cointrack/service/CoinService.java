package com.asd.cointrack.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asd.cointrack.model.Coin;
import com.asd.cointrack.repository.CoinRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoinService {

    private final CoinRepository coinRepository;

    public List<Coin> getAllCoins() {
        return coinRepository.findAll();
    }

    public Coin getCoinById(String id) {
        return coinRepository.findById(id).orElse(null);
    }

    public List<Coin> getCoinsByName(String name) {
        return coinRepository.findByName(name);
    }

    public List<Coin> getCoinsByYear(int year) {
        return coinRepository.findByYear(year);
    }

    public Coin createCoin(Coin coin) {
        return coinRepository.save(coin);
    }

    public Coin updateCoin(String id, Coin updated) {
        Coin existing = coinRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        existing.setName(updated.getName());
        existing.setYear(updated.getYear());
        existing.setMaterial(updated.getMaterial());
        existing.setWeight(updated.getWeight());
        existing.setDiameter(updated.getDiameter());
        existing.setHeight(updated.getHeight());
        existing.setPrice(updated.getPrice());
        existing.setConservationObverse(updated.getConservationObverse());
        existing.setConservationReverse(updated.getConservationReverse());
        existing.setDegree(updated.getDegree());
        existing.setNote(updated.getNote());
        existing.setPhotoPathObverse(updated.getPhotoPathObverse());
        existing.setPhotoPathReverse(updated.getPhotoPathReverse());

        return coinRepository.save(existing);
    }

    public void deleteCoin(String id) {
        coinRepository.deleteById(id);
    }
}

