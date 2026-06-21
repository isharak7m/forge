package com.fitmind.service;

import com.fitmind.dto.nutrition.DailyNutritionSummary;
import com.fitmind.dto.nutrition.FoodLogRequest;
import com.fitmind.dto.nutrition.FoodLogResponse;
import com.fitmind.dto.nutrition.MacroDistribution;
import com.fitmind.entity.FoodLog;
import com.fitmind.entity.User;
import com.fitmind.entity.enums.MealCategory;
import com.fitmind.exception.ResourceNotFoundException;
import com.fitmind.repository.FoodLogRepository;
import com.fitmind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final FoodLogRepository foodLogRepository;
    private final UserRepository userRepository;

    public FoodLogResponse logFood(Long userId, FoodLogRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FoodLog foodLog = FoodLog.builder()
                .user(user)
                .foodName(request.getFoodName())
                .mealCategory(request.getMealCategory())
                .date(request.getDate())
                .servingSize(request.getServingSize())
                .unit(request.getUnit())
                .calories(request.getCalories())
                .proteinG(request.getProteinG())
                .carbsG(request.getCarbsG())
                .fatG(request.getFatG())
                .fiberG(request.getFiberG())
                .build();

        FoodLog saved = foodLogRepository.save(foodLog);
        return mapToResponse(saved);
    }

    public List<FoodLogResponse> getLogs(Long userId, LocalDate date, MealCategory category) {
        List<FoodLog> logs;
        if (category != null) {
            logs = foodLogRepository.findByUserIdAndDateAndMealCategory(userId, date, category);
        } else {
            logs = foodLogRepository.findByUserIdAndDateOrderByLoggedAtAsc(userId, date);
        }
        return logs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public void deleteLog(Long userId, Long logId) {
        FoodLog log = foodLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Food log not found"));
        
        if (!log.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this log");
        }
        
        foodLogRepository.delete(log);
    }

    public DailyNutritionSummary getDailyAnalytics(Long userId, LocalDate date) {
        List<FoodLog> logs = foodLogRepository.findByUserIdAndDateOrderByLoggedAtAsc(userId, date);
        
        double totalCalories = logs.stream().mapToDouble(FoodLog::getCalories).sum();
        double totalProtein = logs.stream().mapToDouble(FoodLog::getProteinG).sum();
        double totalCarbs = logs.stream().mapToDouble(FoodLog::getCarbsG).sum();
        double totalFat = logs.stream().mapToDouble(FoodLog::getFatG).sum();
        double totalFiber = logs.stream().mapToDouble(FoodLog::getFiberG).sum();

        List<FoodLogResponse> meals = logs.stream().map(this::mapToResponse).collect(Collectors.toList());
        
        return DailyNutritionSummary.builder()
                .date(date)
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalCarbs(totalCarbs)
                .totalFat(totalFat)
                .totalFiber(totalFiber)
                .consistencyScore(logs.isEmpty() ? 0.0 : 100.0) // For a single day, it's 0 or 100
                .meals(meals)
                .build();
    }

    public List<DailyNutritionSummary> getWeeklyAnalytics(Long userId, int week, int year) {
        // Simplified: just get last 7 days from today for demonstration
        // A full implementation would calculate dates from ISO week and year
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(6);
        return getAnalyticsForRange(userId, from, to);
    }

    public List<DailyNutritionSummary> getMonthlyAnalytics(Long userId, int month, int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        if (to.isAfter(LocalDate.now())) {
            to = LocalDate.now();
        }
        return getAnalyticsForRange(userId, from, to);
    }

    private List<DailyNutritionSummary> getAnalyticsForRange(Long userId, LocalDate from, LocalDate to) {
        List<DailyNutritionSummary> summaries = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            summaries.add(getDailyAnalytics(userId, current));
            current = current.plusDays(1);
        }
        return summaries;
    }

    public MacroDistribution getMacroDistribution(Long userId, LocalDate from, LocalDate to) {
        Double totalCalories = foodLogRepository.sumCaloriesByUserIdAndDateBetween(userId, from, to);
        Double totalProtein = foodLogRepository.sumProteinByUserIdAndDateBetween(userId, from, to);
        Double totalCarbs = foodLogRepository.sumCarbsByUserIdAndDateBetween(userId, from, to);
        Double totalFat = foodLogRepository.sumFatByUserIdAndDateBetween(userId, from, to);

        totalCalories = totalCalories != null ? totalCalories : 0.0;
        totalProtein = totalProtein != null ? totalProtein : 0.0;
        totalCarbs = totalCarbs != null ? totalCarbs : 0.0;
        totalFat = totalFat != null ? totalFat : 0.0;

        double proteinCals = totalProtein * 4;
        double carbCals = totalCarbs * 4;
        double fatCals = totalFat * 9;
        
        double calcTotalCals = proteinCals + carbCals + fatCals;
        
        double pPct = calcTotalCals > 0 ? (proteinCals / calcTotalCals) * 100 : 0;
        double cPct = calcTotalCals > 0 ? (carbCals / calcTotalCals) * 100 : 0;
        double fPct = calcTotalCals > 0 ? (fatCals / calcTotalCals) * 100 : 0;

        return MacroDistribution.builder()
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalCarbs(totalCarbs)
                .totalFat(totalFat)
                .proteinCalories(proteinCals)
                .carbCalories(carbCals)
                .fatCalories(fatCals)
                .proteinPct(pPct)
                .carbsPct(cPct)
                .fatPct(fPct)
                .build();
    }

    private FoodLogResponse mapToResponse(FoodLog log) {
        return FoodLogResponse.builder()
                .id(log.getId())
                .date(log.getDate())
                .mealCategory(log.getMealCategory())
                .foodName(log.getFoodName())
                .servingSize(log.getServingSize())
                .unit(log.getUnit())
                .calories(log.getCalories())
                .proteinG(log.getProteinG())
                .carbsG(log.getCarbsG())
                .fatG(log.getFatG())
                .fiberG(log.getFiberG())
                .loggedAt(log.getLoggedAt())
                .build();
    }
}
