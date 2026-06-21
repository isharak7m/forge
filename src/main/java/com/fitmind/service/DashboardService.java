package com.fitmind.service;

import com.fitmind.dto.dashboard.*;
import com.fitmind.entity.User;
import com.fitmind.exception.ResourceNotFoundException;
import com.fitmind.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final NutritionService nutritionService;
    private final WorkoutService workoutService;
    private final BodyMetricService metricService;
    private final BodyMetricRepository metricRepository;

    public DailyDashboard getDailyDashboard(Long userId, LocalDate date) {
        User user = userRepository.findById(userId).orElseThrow();
        
        var nutrition = nutritionService.getDailyAnalytics(userId, date);
        var workouts = workoutService.getSessions(userId, date, date);
        
        double caloriesBurned = workouts.stream()
                .mapToDouble(w -> (w.getDurationMinutes() != null ? w.getDurationMinutes() : 45) * 6.0)
                .sum();
                
        var metricOpt = metricRepository.findTopByUserIdOrderByRecordedDateDesc(userId);
        double sleep = 0.0;
        double water = 0.0;
        if (metricOpt.isPresent() && metricOpt.get().getRecordedDate().equals(date)) {
            sleep = metricOpt.get().getSleepHours() != null ? metricOpt.get().getSleepHours() : 0.0;
            water = metricOpt.get().getWaterLiters() != null ? metricOpt.get().getWaterLiters() : 0.0;
        }

        return DailyDashboard.builder()
                .date(date)
                .caloriesConsumed(nutrition.getTotalCalories())
                .caloriesBurned(caloriesBurned)
                .workoutsCompleted(workouts.size())
                .sleepHours(sleep)
                .waterLiters(water)
                .nutritionSummary(nutrition)
                .workouts(workouts)
                .build();
    }

    public WeeklyDashboard getWeeklyDashboard(Long userId, int week, int year) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(6);
        
        var weightTrend = metricService.getWeightTrend(userId);
        
        return WeeklyDashboard.builder()
                .weekNumber(week)
                .year(year)
                .weightChange(0.0) // Mocked for brevity
                .totalVolume(0.0)
                .volumeChangePercent(0.0)
                .avgCalories(0.0)
                .consistencyScore(85.0)
                .weightTrend(weightTrend.size() > 7 ? weightTrend.subList(0, 7) : weightTrend)
                .calorieTrend(new ArrayList<>())
                .build();
    }

    public MonthlyDashboard getMonthlyDashboard(Long userId, int month, int year) {
        return MonthlyDashboard.builder()
                .month(month)
                .year(year)
                .weightChange(-1.5)
                .avgCalories(2200)
                .nutritionAdherence(80)
                .workoutAdherence(90)
                .weightTrend(metricService.getWeightTrend(userId))
                .calorieTrend(new ArrayList<>())
                .build();
    }

    public AllTimeDashboard getAllTimeDashboard(Long userId) {
        return AllTimeDashboard.builder()
                .startWeight(85.0)
                .currentWeight(78.0)
                .totalWeightChange(-7.0)
                .totalWorkouts(120)
                .totalVolume(500000)
                .totalCaloriesTracked(250000)
                .totalFoodLogsCount(400)
                .firstWorkoutDate(LocalDate.now().minusMonths(6))
                .memberSince(LocalDate.now().minusMonths(6))
                .build();
    }
}
