package com.fitmind.dto.dashboard;

import com.fitmind.dto.nutrition.DailyNutritionSummary;
import com.fitmind.dto.workout.WorkoutSessionResponse;
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
