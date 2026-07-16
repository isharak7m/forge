package com.fitmind.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDatabaseItem {
    private String name;
    private double caloriesPer100g;
    private double proteinPer100g;
    private double carbsPer100g;
    private double fatPer100g;
    private double fiberPer100g;

    private Double vitaminAPer100g;
    private Double vitaminCPer100g;
    private Double vitaminDPer100g;
    private Double vitaminEPer100g;
    private Double vitaminKPer100g;
    private Double vitaminB1Per100g;
    private Double vitaminB2Per100g;
    private Double vitaminB3Per100g;
    private Double vitaminB5Per100g;
    private Double vitaminB6Per100g;
    private Double vitaminB7Per100g;
    private Double vitaminB9Per100g;
    private Double vitaminB12Per100g;

    private Double calciumPer100g;
    private Double ironPer100g;
    private Double magnesiumPer100g;
    private Double potassiumPer100g;
    private Double sodiumPer100g;
    private Double zincPer100g;
    private Double copperPer100g;
    private Double manganesePer100g;
    private Double seleniumPer100g;
    private Double phosphorusPer100g;
    private Double iodinePer100g;
    private Double chromiumPer100g;
}
