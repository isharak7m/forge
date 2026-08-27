package com.forge.dto.nutrition;

import com.forge.entity.enums.MealCategory;
import com.forge.entity.enums.ServingUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FoodLogRequest {

    @NotBlank(message = "Food name is required")
    private String foodName;

    @NotNull(message = "Meal category is required")
    private MealCategory mealCategory;

    private LocalDate date = LocalDate.now();
    private Double servingSize;
    private ServingUnit unit;

    private Double calories = 0.0;
    private Double proteinG = 0.0;
    private Double carbsG = 0.0;
    private Double fatG = 0.0;
    private Double fiberG = 0.0;

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
}
