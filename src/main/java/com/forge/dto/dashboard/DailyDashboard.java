package com.forge.dto.dashboard;

import com.forge.dto.nutrition.DailyNutritionSummary;
import com.forge.dto.workout.WorkoutSessionResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DailyDashboard {
    private LocalDate date;
    private double caloriesConsumed;
    private double caloriesBurned;
    private int workoutsCompleted;
    private double sleepHours;
    private double waterLiters;
    private DailyNutritionSummary nutritionSummary;
    private List<WorkoutSessionResponse> workouts;
}
