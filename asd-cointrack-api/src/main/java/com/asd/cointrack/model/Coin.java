package com.asd.cointrack.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "coins")

public class Coin {
    @Id
    private String ID;
    private String name;
    private int year;
    private String material;
    private double weight;
    private double diameter;
    private double height;
    private double price;
    private OptionConservation conservationObverse;
    private OptionConservation conservationReverse;
    private NumismaticRarity degree;
    private String note;
    private String photoPathObverse;
    private String photoPathReverse;
    
    public Coin(String name, int year, String material, double weight, double diameter, double height, double price,
            OptionConservation conservationObverse, OptionConservation conservationReverse, NumismaticRarity degree,
            String note, String photoPathObverse, String photoPathReverse) {
        this.name = name;
        this.year = year;
        this.material = material;
        this.weight = weight;
        this.diameter = diameter;
        this.height = height;
        this.price = price;
        this.conservationObverse = conservationObverse;
        this.conservationReverse = conservationReverse;
        this.degree = degree;
        this.note = note;
        this.photoPathObverse = photoPathObverse;
        this.photoPathReverse = photoPathReverse;
    }

}

