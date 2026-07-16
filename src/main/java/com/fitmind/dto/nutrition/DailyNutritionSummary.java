package com.fitmind.dto.nutrition;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DailyNutritionSummary {
    private LocalDate date;
    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private double totalFiber;
    private double consistencyScore;
    private List<FoodLogResponse> meals;
    private Map<String, Double> micronutrients;
}
