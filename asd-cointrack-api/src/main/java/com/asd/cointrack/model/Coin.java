package com.asd.cointrack.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.asd.cointrack.validation.MaxCurrentYear;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "name must not be blank")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    @Min(value = 0, message = "year must be greater than or equal to 0")
    @MaxCurrentYear
    @Indexed
    private int year;

    @NotBlank(message = "material must not be blank")
    @Size(max = 100, message = "material must not exceed 100 characters")
    @Indexed
    private String material;

    @Positive(message = "weight must be greater than 0")
    private double weight;

    @Positive(message = "diameter must be greater than 0")
    private double diameter;

    @Positive(message = "height must be greater than 0")
    private double height;

    @PositiveOrZero(message = "price must be greater than or equal to 0")
    @Indexed
    private double price;

    @NotNull(message = "conservationObverse must not be null")
    private OptionConservation conservationObverse;

    @NotNull(message = "conservationReverse must not be null")
    private OptionConservation conservationReverse;

    @NotNull(message = "degree must not be null")
    @Indexed
    private NumismaticRarity degree;

    @Size(max = 1000, message = "note must not exceed 1000 characters")
    private String note;
    private String photoPathObverse;
    private String photoPathReverse;

    @Indexed
    private String collectionId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

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
