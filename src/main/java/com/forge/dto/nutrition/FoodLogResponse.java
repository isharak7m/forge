package com.forge.dto.nutrition;

import com.forge.entity.enums.MealCategory;
import com.forge.entity.enums.ServingUnit;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FoodLogResponse {
    private Long id;
    private LocalDate date;
    private MealCategory mealCategory;
    private String foodName;
    private Double servingSize;
    private ServingUnit unit;
    private Double calories;
    private Double proteinG;
    private Double carbsG;
    private Double fatG;
    private Double fiberG;

    private Double vitaminA;
    private Double vitaminC;
    private Double vitaminD;
    private Double vitaminE;
    private Double vitaminK;
    private Double vitaminB1;
    private Double vitaminB2;
    private Double vitaminB3;
    private Double vitaminB5;
    private Double vitaminB6;
    private Double vitaminB7;
    private Double vitaminB9;
    private Double vitaminB12;

    private Double calcium;
    private Double iron;
    private Double magnesium;
    private Double potassium;
    private Double sodium;
    private Double zinc;
    private Double copper;
    private Double manganese;
    private Double selenium;
    private Double phosphorus;
    private Double iodine;
    private Double chromium;

    private LocalDateTime loggedAt;
}
