package com.fitmind.dto.dashboard;

import com.fitmind.dto.workout.PersonalRecord;
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
