package com.forge.dto.dashboard;

import com.forge.dto.workout.PersonalRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class AllTimeDashboard {
    private double startWeight;
    private double currentWeight;
    private double totalWeightChange;
    private int totalWorkouts;
    private double totalVolume;
    private double totalCaloriesTracked;
    private int totalFoodLogsCount;
    private Map<String, PersonalRecord> bestLifts;
    private LocalDate firstWorkoutDate;
    private LocalDate memberSince;
}
