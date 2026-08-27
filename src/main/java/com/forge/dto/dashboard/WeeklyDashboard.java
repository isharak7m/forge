package com.forge.dto.dashboard;

import com.forge.dto.metrics.TrendPoint;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WeeklyDashboard {
    private int weekNumber;
    private int year;
    private double weightChange;
    private double totalVolume;
    private double volumeChangePercent;
    private double avgCalories;
    private double consistencyScore;
    private List<TrendPoint> weightTrend;
    private List<TrendPoint> calorieTrend;
}
