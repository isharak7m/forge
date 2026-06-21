package com.fitmind.dto.nutrition;

import com.fitmind.entity.enums.MealCategory;
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
    private String unit;

    private Double calories = 0.0;
    private Double proteinG = 0.0;
    private Double carbsG = 0.0;
    private Double fatG = 0.0;
    private Double fiberG = 0.0;
}
