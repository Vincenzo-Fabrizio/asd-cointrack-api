package com.asd.cointrack.service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.asd.cointrack.dto.CoinAuditInfo;
import com.asd.cointrack.dto.CoinsByDegreeStats;
import com.asd.cointrack.dto.CoinsByYearStats;
import com.asd.cointrack.dto.CoinsSummaryStats;
import com.asd.cointrack.dto.CoinsByMaterialStats;
import com.asd.cointrack.model.Coin;
import com.asd.cointrack.model.NumismaticRarity;
import com.asd.cointrack.model.OptionConservation;
import com.asd.cointrack.repository.CoinRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoinService {

    private final CoinRepository coinRepository;
    private final MongoTemplate mongoTemplate;

    public Page<Coin> getAllCoins(Pageable pageable) {
        Pageable sanitized = sanitizePageable(pageable);
        return coinRepository.findAll(sanitized);
    }

    public Coin getCoinById(String id) {
        return coinRepository.findById(id).orElse(null);
    }

    public Page<Coin> searchCoins(String name, Integer year, Pageable pageable) {
        Pageable sanitized = sanitizePageable(pageable);

        if (name != null && !name.isBlank()) {
            return coinRepository.findByName(name, sanitized);
        }

        if (year != null) {
            return coinRepository.findByYear(year, sanitized);
        }

        return coinRepository.findAll(sanitized);
    }

    public Page<Coin> advancedSearch(
            String name,
            String material,
            NumismaticRarity degree,
            List<NumismaticRarity> degreesIn,
            List<OptionConservation> conservationObverseIn,
            Integer minYear,
            Integer maxYear,
            Double minPrice,
            Double maxPrice,
            Pageable pageable) {

        validateRanges(minYear, maxYear, minPrice, maxPrice);

        Pageable sanitized = sanitizePageable(pageable);

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (name != null && !name.isBlank()) {
            criteria = criteria.and("name")
                    .regex(Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE));
        }
        if (material != null && !material.isBlank()) {
            criteria = criteria.and("material")
                    .regex(Pattern.compile(Pattern.quote(material), Pattern.CASE_INSENSITIVE));
        }
        if (degreesIn != null && !degreesIn.isEmpty()) {
            criteria = criteria.and("degree").in(degreesIn);
        } else if (degree != null) {
            criteria = criteria.and("degree").is(degree);
        }
        if (conservationObverseIn != null && !conservationObverseIn.isEmpty()) {
            criteria = criteria.and("conservationObverse").in(conservationObverseIn);
        }
        if (minYear != null) {
            criteria = criteria.and("year").gte(minYear);
        }
        if (maxYear != null) {
            criteria = criteria.and("year").lte(maxYear);
        }
        if (minPrice != null) {
            criteria = criteria.and("price").gte(minPrice);
        }
        if (maxPrice != null) {
            criteria = criteria.and("price").lte(maxPrice);
        }

        if (!criteria.getCriteriaObject().isEmpty()) {
            query.addCriteria(criteria);
        }

        long total = mongoTemplate.count(query, Coin.class);
        query.with(sanitized);
        List<Coin> content = mongoTemplate.find(query, Coin.class);

        return new PageImpl<>(content, sanitized, total);
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

    public CoinAuditInfo getCoinAuditInfo(String id) {
        Coin coin = getCoinById(id);
        if (coin == null) {
            return null;
        }
        return new CoinAuditInfo(
                coin.getID(),
                coin.getName(),
                coin.getCreatedAt(),
                coin.getUpdatedAt(),
                coin.getCreatedBy(),
                coin.getUpdatedBy());
    }

    public CoinsSummaryStats getSummaryStats() {
        List<Coin> coins = coinRepository.findAll();
        if (coins.isEmpty()) {
            return new CoinsSummaryStats(0L, 0.0, 0.0, null, null);
        }

        long totalCount = coins.size();
        double totalPrice = coins.stream().mapToDouble(Coin::getPrice).sum();
        double averagePrice = totalPrice / totalCount;

        int minYear = coins.stream().mapToInt(Coin::getYear).min().orElse(0);
        int maxYear = coins.stream().mapToInt(Coin::getYear).max().orElse(0);

        return new CoinsSummaryStats(totalCount, totalPrice, averagePrice, minYear, maxYear);
    }

    public List<CoinsByYearStats> getStatsByYear() {
        List<Coin> coins = coinRepository.findAll();
        Map<Integer, Long> grouped = coins.stream()
                .collect(Collectors.groupingBy(Coin::getYear, Collectors.counting()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new CoinsByYearStats(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<CoinsByDegreeStats> getStatsByDegree() {
        List<Coin> coins = coinRepository.findAll();
        Map<NumismaticRarity, Long> grouped = coins.stream()
                .collect(Collectors.groupingBy(Coin::getDegree, Collectors.counting()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new CoinsByDegreeStats(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<CoinsByMaterialStats> getStatsByMaterial() {
        List<Coin> coins = coinRepository.findAll();
        Map<String, Long> grouped = coins.stream()
                .collect(Collectors.groupingBy(Coin::getMaterial, Collectors.counting()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new CoinsByMaterialStats(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<Coin> getTopExpensiveCoins(int limit) {
        int pageSize = Math.min(Math.max(limit, 1), 100);
        PageRequest pageRequest = PageRequest.of(0, pageSize,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "price"));
        return coinRepository.findAll(pageRequest).getContent();
    }

    private Pageable sanitizePageable(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = pageable.getPageSize();
        int maxSize = 100;
        if (size <= 0) {
            size = 20;
        } else if (size > maxSize) {
            size = maxSize;
        }
        return PageRequest.of(page, size, pageable.getSort());
    }

    private void validateRanges(Integer minYear, Integer maxYear, Double minPrice, Double maxPrice) {
        if (minYear != null && maxYear != null && minYear > maxYear) {
            throw new IllegalArgumentException("minYear must be less than or equal to maxYear");
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
        }
    }
}
