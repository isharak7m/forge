package com.fitmind.dto.nutrition;

import com.fitmind.entity.enums.MealCategory;
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
    private String unit;
    private Double calories;
    private Double proteinG;
    private Double carbsG;
    private Double fatG;
    private Double fiberG;
    private LocalDateTime loggedAt;
}
