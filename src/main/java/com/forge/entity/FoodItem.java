package com.forge.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_items", indexes = {
    @Index(name = "idx_food_item_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category;

    private Double caloriesPer100g;
    private Double proteinPer100g;
    private Double carbsPer100g;
    private Double fatPer100g;
    private Double fiberPer100g;

    @Column(name = "vitamin_a_per100g") private Double vitaminAPer100g;
    @Column(name = "vitamin_c_per100g") private Double vitaminCPer100g;
    @Column(name = "vitamin_d_per100g") private Double vitaminDPer100g;
    @Column(name = "vitamin_e_per100g") private Double vitaminEPer100g;
    @Column(name = "vitamin_k_per100g") private Double vitaminKPer100g;
    @Column(name = "vitamin_b1_per100g") private Double vitaminB1Per100g;
    @Column(name = "vitamin_b2_per100g") private Double vitaminB2Per100g;
    @Column(name = "vitamin_b3_per100g") private Double vitaminB3Per100g;
    @Column(name = "vitamin_b5_per100g") private Double vitaminB5Per100g;
    @Column(name = "vitamin_b6_per100g") private Double vitaminB6Per100g;
    @Column(name = "vitamin_b7_per100g") private Double vitaminB7Per100g;
    @Column(name = "vitamin_b9_per100g") private Double vitaminB9Per100g;
    @Column(name = "vitamin_b12_per100g") private Double vitaminB12Per100g;

    @Column(name = "calcium_per100g") private Double calciumPer100g;
    @Column(name = "iron_per100g") private Double ironPer100g;
    @Column(name = "magnesium_per100g") private Double magnesiumPer100g;
    @Column(name = "potassium_per100g") private Double potassiumPer100g;
    @Column(name = "sodium_per100g") private Double sodiumPer100g;
    @Column(name = "zinc_per100g") private Double zincPer100g;
    @Column(name = "copper_per100g") private Double copperPer100g;
    @Column(name = "manganese_per100g") private Double manganesePer100g;
    @Column(name = "selenium_per100g") private Double seleniumPer100g;
    @Column(name = "phosphorus_per100g") private Double phosphorusPer100g;
    @Column(name = "iodine_per100g") private Double iodinePer100g;
    @Column(name = "chromium_per100g") private Double chromiumPer100g;
}
