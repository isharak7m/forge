package com.fitmind.service;

import com.fitmind.dto.dashboard.*;
import com.fitmind.dto.metrics.TrendPoint;
import com.fitmind.dto.nutrition.DailyNutritionSummary;
import com.fitmind.entity.User;
import com.fitmind.exception.ResourceNotFoundException;
import com.fitmind.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final NutritionService nutritionService;
    private final WorkoutService workoutService;
    private final SleepLogService sleepLogService;
    private final WaterLogService waterLogService;
    private final WeightLogService weightLogService;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final FoodLogRepository foodLogRepository;

    public DailyDashboard getDailyDashboard(Long userId, LocalDate date) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        var nutrition = nutritionService.getDailyAnalytics(userId, date);
        var workouts = workoutService.getSessions(userId, date, date);
        
        double caloriesBurned = workouts.stream()
                .mapToDouble(w -> (w.getDurationMinutes() != null ? w.getDurationMinutes() : 45) * 6.0)
                .sum();
                
        double sleep = sleepLogService.getDailySleepHours(userId, date);
        double water = waterLogService.getDailyWater(userId, date);

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
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
        int clampedWeek = Math.max(1, Math.min(week, 52));
        LocalDate weekStart = firstDayOfYear.with(weekFields.weekOfYear(), clampedWeek).with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        if (weekEnd.isAfter(LocalDate.now())) weekEnd = LocalDate.now();

        LocalDate prevWeekStart = weekStart.minusDays(7);
        LocalDate prevWeekEnd = weekStart.minusDays(1);

        List<DailyNutritionSummary> nutritionSummaries = nutritionService.getWeeklyAnalytics(userId, clampedWeek, year);
        double avgCalories = nutritionSummaries.stream()
                .mapToDouble(DailyNutritionSummary::getTotalCalories)
                .average().orElse(0);

        var volumeByDate = workoutService.getVolumeAnalytics(userId, weekStart, weekEnd);
        double totalVolume = volumeByDate.values().stream().mapToDouble(Double::doubleValue).sum();

        var prevVolumeByDate = workoutService.getVolumeAnalytics(userId, prevWeekStart, prevWeekEnd);
        double prevTotalVolume = prevVolumeByDate.values().stream().mapToDouble(Double::doubleValue).sum();
        double volumeChangePercent = prevTotalVolume > 0 ? ((totalVolume - prevTotalVolume) / prevTotalVolume) * 100 : 0.0;

        long workoutDays = workoutSessionRepository.findDistinctDatesByUserIdAndDateBetween(userId, weekStart, weekEnd).size();
        double consistencyScore = Math.min(100, (workoutDays / 7.0) * 100);

        var weightHistory = weightLogService.getHistory(userId, weekStart, weekEnd);
        double weightChange = 0;
        if (weightHistory.size() >= 2) {
            weightChange = weightHistory.get(weightHistory.size() - 1).getWeightKg() - weightHistory.get(0).getWeightKg();
        }

        List<TrendPoint> weightTrend = weightHistory.stream()
                .map(w -> TrendPoint.builder().date(w.getDate()).value(w.getWeightKg()).label(w.getWeightKg() + " kg").build())
                .collect(Collectors.toList());

        List<TrendPoint> calorieTrend = nutritionSummaries.stream()
                .map(n -> TrendPoint.builder().date(n.getDate()).value(n.getTotalCalories()).label((int) n.getTotalCalories() + " kcal").build())
                .collect(Collectors.toList());

        return WeeklyDashboard.builder()
                .weekNumber(clampedWeek)
                .year(year)
                .weightChange(weightChange)
                .totalVolume(totalVolume)
                .volumeChangePercent(volumeChangePercent)
                .avgCalories(avgCalories)
                .consistencyScore(consistencyScore)
                .weightTrend(weightTrend)
                .calorieTrend(calorieTrend)
                .build();
    }

    public MonthlyDashboard getMonthlyDashboard(Long userId, int month, int year) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        if (monthEnd.isAfter(LocalDate.now())) monthEnd = LocalDate.now();

        List<DailyNutritionSummary> nutritionSummaries = nutritionService.getMonthlyAnalytics(userId, month, year);
        double avgCalories = nutritionSummaries.stream()
                .mapToDouble(DailyNutritionSummary::getTotalCalories)
                .average().orElse(0);

        long totalWorkoutDays = workoutSessionRepository.findDistinctDatesByUserIdAndDateBetween(userId, monthStart, monthEnd).size();
        long totalDaysInMonth = ChronoUnit.DAYS.between(monthStart, monthEnd) + 1;
        double workoutAdherence = Math.min(100, (totalWorkoutDays * 4.0 / totalDaysInMonth) * 100);
        double nutritionAdherence = (double) nutritionSummaries.stream()
                .filter(n -> n.getTotalCalories() > 0).count() / totalDaysInMonth * 100;

        var weightHistory = weightLogService.getHistory(userId, monthStart, monthEnd);
        double weightChange = 0;
        if (weightHistory.size() >= 2) {
            weightChange = weightHistory.get(weightHistory.size() - 1).getWeightKg() - weightHistory.get(0).getWeightKg();
        }

        List<TrendPoint> weightTrend = weightHistory.stream()
                .map(w -> TrendPoint.builder().date(w.getDate()).value(w.getWeightKg()).label(w.getWeightKg() + " kg").build())
                .collect(Collectors.toList());

        List<TrendPoint> calorieTrend = nutritionSummaries.stream()
                .map(n -> TrendPoint.builder().date(n.getDate()).value(n.getTotalCalories()).label((int) n.getTotalCalories() + " kcal").build())
                .collect(Collectors.toList());

        var progressionMap = workoutService.getVolumeAnalytics(userId, monthStart, monthEnd);

        return MonthlyDashboard.builder()
                .month(month)
                .year(year)
                .weightChange(weightChange)
                .avgCalories(avgCalories)
                .nutritionAdherence(nutritionAdherence)
                .workoutAdherence(workoutAdherence)
                .weightTrend(weightTrend)
                .calorieTrend(calorieTrend)
                .exerciseProgressionSummary(progressionMap)
                .build();
    }

    public AllTimeDashboard getAllTimeDashboard(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Double startWeight = weightLogService.getStartWeight(userId);
        Double currentWeight = weightLogService.getCurrentWeight(userId);
        if (startWeight == null) startWeight = user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : 80.0;
        if (currentWeight == null) currentWeight = user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : 80.0;
        double totalWeightChange = currentWeight - startWeight;

        var allSessions = workoutSessionRepository.findByUserIdOrderByDateDesc(userId);
        int totalWorkouts = allSessions.size();
        double totalVolume = allSessions.stream()
                .flatMap(s -> s.getExercises().stream())
                .mapToDouble(e -> {
                    if (e.getWeightKg() == null || e.getReps() == null || e.getSets() == null) return 0;
                    return e.getWeightKg() * e.getReps() * e.getSets();
                }).sum();

        Double totalCals = foodLogRepository.sumCaloriesByUserIdAndDateBetween(userId, LocalDate.now().minusYears(1), LocalDate.now());
        int totalFoodLogs = foodLogRepository.countByUserId(userId).intValue();

        var bestLifts = workoutService.getPersonalRecords(userId);
        var bestLiftsMap = bestLifts.stream()
                .collect(Collectors.toMap(
                        com.fitmind.dto.workout.PersonalRecord::getExerciseName,
                        pr -> pr
                ));

        LocalDate firstWorkout = allSessions.isEmpty() ? LocalDate.now().minusMonths(6) : allSessions.get(allSessions.size() - 1).getDate();
        var firstLog = foodLogRepository.findFirstByUserIdOrderByDateAsc(userId);
        LocalDate memberSince = firstLog.map(l -> l.getDate().atStartOfDay().toLocalDate()).orElse(firstWorkout);

        return AllTimeDashboard.builder()
                .startWeight(startWeight)
                .currentWeight(currentWeight)
                .totalWeightChange(Math.round(totalWeightChange * 100.0) / 100.0)
                .totalWorkouts(totalWorkouts)
                .totalVolume(Math.round(totalVolume * 100.0) / 100.0)
                .totalCaloriesTracked(totalCals != null ? totalCals : 0)
                .totalFoodLogsCount(totalFoodLogs)
                .bestLifts(bestLiftsMap)
                .firstWorkoutDate(firstWorkout)
                .memberSince(memberSince)
                .build();
    }
}
