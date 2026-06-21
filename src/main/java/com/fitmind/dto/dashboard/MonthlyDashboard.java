package com.fitmind.dto.dashboard;

import com.fitmind.dto.metrics.TrendPoint;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MonthlyDashboard {
    private int month;
    private int year;
    private double weightChange;
    private double avgCalories;
    private double nutritionAdherence;
    private double workoutAdherence;
    private List<TrendPoint> weightTrend;
    private List<TrendPoint> calorieTrend;
    private Map<String, Double> exerciseProgressionSummary;
}
